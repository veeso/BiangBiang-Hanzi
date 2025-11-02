//
//  CameraPreview.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import AVFoundation
import SwiftUI

/// Wrapper SwiftUI per mostrare la preview della fotocamera
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill

        if let connection = previewLayer.connection {
            if #available(iOS 17.0, *) {
                // Portrait corresponds to 0 degrees
                if connection.isVideoRotationAngleSupported(0) {
                    connection.videoRotationAngle = 0
                }
            } else {
                if connection.isVideoOrientationSupported {
                    connection.videoOrientation = .portrait
                }
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
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}
