//
//  CameraPreview.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import AVFoundation
import SwiftUI

/// SwiftUI wrapper that renders the live camera preview.
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession
    let cameraModel: CameraModel

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspect
        cameraModel.previewLayer = previewLayer

        if let connection = previewLayer.connection {
            if connection.isVideoRotationAngleSupported(0) {
                connection.videoRotationAngle = 0
            }
        }

        view.layer.addSublayer(previewLayer)
        context.coordinator.previewLayer = previewLayer

        // Ensure initial sizing
        previewLayer.frame = view.bounds

        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            context.coordinator.previewLayer?.frame = uiView.bounds
            cameraModel.previewLayer?.frame = uiView.bounds
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}
