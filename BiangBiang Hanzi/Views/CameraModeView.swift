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
        VStack(spacing: 0) {
            ZStack {
                CameraPreview(session: cameraModel.session)
                    .ignoresSafeArea()

                // Recognized text
                VStack(spacing: 4) {
                    if !cameraModel.pinyinText.isEmpty {
                        Text(cameraModel.pinyinText)
                            .font(.title2)
                            .bold()
                            .foregroundColor(.red)
                            .shadow(radius: 2)
                    }
                    if !cameraModel.recognizedText.isEmpty {
                        Text(cameraModel.recognizedText)
                            .font(.largeTitle)
                            .foregroundColor(.white)
                            .shadow(radius: 2)
                    }
                }
                .padding()
                .background(Color.black.opacity(0.3))
                .cornerRadius(12)
                .padding(.top, 80)

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
}

#Preview {
    CameraModeView().environmentObject(AppSettings())
}
