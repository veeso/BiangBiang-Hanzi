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
        GeometryReader { geo in
            ZStack {
                CameraPreview(session: cameraModel.session)
                    .ignoresSafeArea()

                // Recognized text
                ForEach(cameraModel.recognizedTexts) { box in
                    if let pinyin = cameraModel.pinyinMap[box.id] {
                        let frame = visionToViewRect(
                            box.boundingBox,
                            in: geo.size
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

                Spacer()
                VStack {
                    Spacer()
                    HStack {
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

                        }
                    }
                    .padding(.bottom, 40)
                }
            }
        }.task {
            await cameraModel.checkPermissionsAndStart()
        }
    }

    private func visionToViewRect(_ rect: CGRect, in size: CGSize) -> CGRect {
        let viewWidth = size.width
        let viewHeight = size.height

        let x = rect.minX * viewWidth
        let y = rect.midY * viewHeight
        let width = rect.width * viewWidth
        let height = rect.height * viewHeight

        return CGRect(x: x, y: y, width: width, height: height)
    }

}

#Preview {
    CameraModeView().environmentObject(AppSettings())
}
