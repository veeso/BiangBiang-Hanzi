//
//  CameraModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import AVFoundation
import PhotosUI
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
        @State private var selectedItem: PhotosPickerItem?

        var body: some View {
            GeometryReader { geo in
                ZStack {
                    // 🎥 show live camera (always)
                    CameraPreview(
                        session: cameraModel.session,
                        cameraModel: cameraModel
                    )
                    .ignoresSafeArea()

                    if let image = cameraModel.capturedImage {
                        // 📸 show taken photo
                        Image(uiImage: image)
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .background(Color.black)
                            .ignoresSafeArea()
                    }

                    // Recognized text
                    ForEach(cameraModel.recognizedTexts) { box in
                        if let pinyin = cameraModel.pinyinMap[box.id] {
                            TextOverlay(
                                cameraModel: cameraModel,
                                hanzi: box.text,
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
                                            "xmark.circle.fill"
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
                                // Toggle Hanzi/Pinyin
                                Button(action: {
                                    cameraModel.showPinyin.toggle()
                                }) {
                                    Image(
                                        "BiangBiang"
                                    )
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 28, height: 28)
                                    .font(.system(size: 24, weight: .medium))
                                    .foregroundColor(.white)
                                    .padding()
                                    .background(
                                        Circle()
                                            .fill(
                                                cameraModel.showPinyin
                                                    ? .red.opacity(0.8)
                                                    : .gray.opacity(0.8)
                                            )
                                            .shadow(radius: 4)
                                    )
                                    .animation(
                                        .easeInOut(duration: 0.2),
                                        value: cameraModel.showPinyin
                                    )
                                    .accessibilityLabel("Toggle Pinyin")
                                }
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
                                // Photo picker button
                                PhotosPicker(
                                    selection: $selectedItem,
                                    matching: .images,
                                    photoLibrary: .shared()
                                ) {
                                    Image(systemName: "photo.on.rectangle")
                                        .font(.system(size: 24))
                                        .foregroundColor(.white)
                                        .padding()
                                        .background(
                                            Circle()
                                                .fill(.gray)
                                                .shadow(radius: 4)
                                        )
                                        .accessibilityLabel(
                                            "Scan photo from gallery"
                                        )

                                }
                                .onChange(of: selectedItem) { _, newItem in
                                    guard let newItem else { return }
                                    Task {
                                        // Load as Data and build UIImage
                                        if let data =
                                            try? await newItem.loadTransferable(
                                                type: Data.self
                                            ),
                                            let image = UIImage(data: data)
                                        {
                                            cameraModel.recognizeGalleryImage(
                                                image
                                            )
                                        }
                                        // reset selection to allow re-selecting same photo
                                        selectedItem = nil
                                    }
                                }
                            }
                        }
                        .padding(.bottom, 40)
                    }
                    if cameraModel.showCopiedToast {
                        CopyToast()
                            .transition(
                                .move(edge: .bottom).combined(with: .opacity)
                            )
                            .frame(maxWidth: .infinity)
                            .padding(.bottom, 40)
                            .animation(
                                .easeOut(duration: 0.25),
                                value: cameraModel.showCopiedToast
                            )
                            .allowsHitTesting(false)
                            .alignmentGuide(.bottom) { d in d[.bottom] }
                    }
                }
            }.task {
                await cameraModel.checkPermissionsAndStart()
            }
        }

        private struct TextOverlay: View {
            @State private var isCopied = false

            @ObservedObject var cameraModel: CameraModel
            let hanzi: String
            let pinyin: String
            let boundingBox: RecognizedTextBox
            let viewSize: CGSize

            var body: some View {
                let frame = visionToViewRect(
                    boundingBox.boundingBox,
                    in: viewSize
                )
                // center position

                let x = max(0, frame.minX + (frame.width * 0.5))
                // dynamic font size
                let scaleRatio =
                    cameraModel.showPinyin
                    ? CGFloat(hanzi.count) / CGFloat(pinyin.count) : 1.0
                let scaleFactor = min(max(scaleRatio, 0.6), 1.0)
                let fontSize = max(8, frame.height * scaleFactor)
                let textToDisplay = cameraModel.showPinyin ? pinyin : hanzi

                // calculate y and remove half of the font size
                let y = max(
                    0,
                    frame.minY - (frame.height * 0.5) - (fontSize * 0.2)
                )

                ZStack {
                    Button {
                        copy(textToDisplay)
                    } label: {
                        VStack(spacing: 1) {
                            Text(textToDisplay)
                                .font(.system(size: fontSize, weight: .medium))
                                .foregroundColor(isCopied ? .blue : .black)
                                .minimumScaleFactor(0.5)
                                .lineLimit(1)
                                .animation(
                                    .easeOut(duration: 0.25),
                                    value: isCopied
                                )
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
                    }
                    .buttonStyle(.plain)  // disable default blue style
                    .frame(width: frame.width, height: frame.height)
                    .position(x: x, y: y)
                }
            }

            private func copy(_ text: String) {
                UIPasteboard.general.string = text
                UIImpactFeedbackGenerator(style: .light).impactOccurred()

                isCopied = true
                cameraModel.showCopiedToast = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                    isCopied = false
                }

                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                    withAnimation(.easeOut(duration: 0.2)) {
                        cameraModel.showCopiedToast = false
                    }
                }
            }

            private func visionToViewRect(_ rect: CGRect, in size: CGSize)
                -> CGRect
            {
                if let previewLayer = cameraModel.previewLayer {
                    let videoRect = previewLayer.layerRectConverted(
                        fromMetadataOutputRect: rect
                    )
                    // X must be flipped for some reasons...
                    let flippedX =
                        size.width - videoRect.origin.x - videoRect.width
                    return CGRect(
                        x: flippedX,
                        y: videoRect.origin.y,
                        width: videoRect.width,
                        height: videoRect.height
                    )
                } else {
                    // Fallback to base formula
                    let viewWidth = size.width
                    let viewHeight = size.height

                    let x = rect.minX * viewWidth
                    let y = rect.midY * viewHeight
                    let width = rect.width * viewWidth
                    let height = rect.height * viewHeight

                    let rect = CGRect(x: x, y: y, width: width, height: height)
                    return rect

                }

            }

        }
    }

    struct CopyToast: View {
        var body: some View {
            Text("Text copied")
                .font(.system(size: 14, weight: .semibold))
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(.ultraThinMaterial)
                .cornerRadius(12)
                .shadow(radius: 6)
        }
    }
}

#Preview {
    CameraModeView().environmentObject(AppSettings())
}
