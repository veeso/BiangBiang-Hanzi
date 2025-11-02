//
//  CameraModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import AVFoundation
import SwiftUI
import Vision

struct CameraModeView: View {
    @EnvironmentObject var settings: AppSettings
    @StateObject private var cameraModel = CameraModel()

    var body: some View {
        if cameraModel.missingCameraPermission {
            CameraPermissionView()
        } else {
            CameraLiveView(cameraModel: cameraModel)
        }
    }

    private struct CameraPermissionView: View {
        var body: some View {
            VStack(spacing: 16) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 48))
                    .foregroundColor(.yellow)
                Text("Camera access is disabled")
                    .font(.headline)
                Text(
                    "Please enable camera permissions in Settings > Privacy > Camera."
                )
                .font(.subheadline)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
            }
            .padding()
            .background(.ultraThinMaterial)
            .cornerRadius(16)
            .shadow(radius: 10)
            .padding()
        }
    }

    private struct CameraLiveView: View {
        @ObservedObject var cameraModel: CameraModel

        var body: some View {
            GeometryReader { geo in
                ZStack {
                    // 🎥 show live camera (always)
                    CameraPreview(session: cameraModel.session)
                        .ignoresSafeArea()

                    if let image = cameraModel.capturedImage {
                        // 📸 show taken photo
                        Image(uiImage: image)
                            .resizable()
                            .scaledToFill()
                            .ignoresSafeArea()
                    }

                    // Recognized text
                    ForEach(cameraModel.recognizedTexts) { box in
                        if let pinyin = cameraModel.pinyinMap[box.id] {
                            TextOverlay(
                                cameraModel: cameraModel,
                                pinyin: pinyin,
                                boundingBox: box,
                                viewSize: geo.size
                            )
                        }
                    }
                    Spacer()
                    VStack {
                        Spacer()
                        HStack {
                            if cameraModel.capturedImage != nil {
                                Button(action: cameraModel.deleteCapturedImage)
                                {
                                    Image(
                                        systemName:
                                            "arrow.uturn.backward.circle.fill"
                                    )
                                    .font(.system(size: 24))
                                    .foregroundColor(.white)
                                    .padding()
                                    .background(
                                        Circle()
                                            .fill(.gray)
                                            .shadow(radius: 4)
                                    )
                                    .accessibilityLabel("Retake photo")
                                }
                            } else {
                                Button(action: cameraModel.capturePhoto) {
                                    Image(systemName: "camera.fill")
                                        .font(.system(size: 24))
                                        .foregroundColor(.white)
                                        .padding()
                                        .background(
                                            Circle()
                                                .fill(.gray)
                                                .shadow(radius: 4)
                                        )
                                        .accessibilityLabel("Take photo")
                                }
                            }
                        }
                        .padding(.bottom, 40)
                    }
                }
            }.task {
                await cameraModel.checkPermissionsAndStart()
            }
        }

        private struct TextOverlay: View {
            @ObservedObject var cameraModel: CameraModel
            let pinyin: String
            let boundingBox: RecognizedTextBox
            let viewSize: CGSize

            var body: some View {
                if let pinyin = cameraModel.pinyinMap[boundingBox.id] {
                    let frame = visionToViewRect(
                        boundingBox.boundingBox,
                        in: viewSize
                    )

                    VStack(spacing: 1) {
                        Text(pinyin)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.black)

                    }
                    .padding(4)
                    .background(
                        RoundedRectangle(cornerRadius: 6)
                            .fill(Color.white.opacity(0.9))
                            .shadow(
                                color: .black.opacity(0.2),
                                radius: 1,
                                x: 0,
                                y: 1
                            )
                    )
                    .position(x: frame.minX, y: frame.minY)
                }
            }

            private func visionToViewRect(_ rect: CGRect, in size: CGSize)
                -> CGRect
            {
                let viewWidth = size.width
                let viewHeight = size.height

                let x = rect.minX * viewWidth
                let y = rect.midY * viewHeight
                let width = rect.width * viewWidth
                let height = rect.height * viewHeight

                return CGRect(x: x, y: y, width: width, height: height)
            }

        }
    }
}

#Preview {
    CameraModeView().environmentObject(AppSettings())
}
