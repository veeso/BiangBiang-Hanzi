//
//  CameraModel.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import AVFoundation
import Combine
import Foundation
import UIKit
import Vision

/// Text box to put upon as identified by the camera.
struct RecognizedTextBox: Identifiable {
    let id = UUID()
    let text: String
    let boundingBox: CGRect  // Normalized (0-1)
}

@MainActor
class CameraModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    @Published var recognizedTexts: [RecognizedTextBox] = []
    @Published var pinyinMap: [UUID: String] = [:]

    let session = AVCaptureSession()
    private let output = AVCapturePhotoOutput()

    func capturePhoto() {
        output.capturePhoto(with: AVCapturePhotoSettings(), delegate: self)
    }

    func photoOutput(
        _ output: AVCapturePhotoOutput,
        didFinishProcessingPhoto photo: AVCapturePhoto,
        error: Error?
    ) {
        guard let imageData = photo.fileDataRepresentation(),
            let image = UIImage(data: imageData)
        else { return }
        recognizeText(from: image)
    }

    private func recognizeText(from image: UIImage) {
        guard let cgImage = image.cgImage else { return }
        let request = VNRecognizeTextRequest { [weak self] req, _ in
            guard let self,
                let results = req.results as? [VNRecognizedTextObservation]
            else { return }

            let newTexts: [RecognizedTextBox] = results.compactMap {
                guard let top = $0.topCandidates(1).first else { return nil }
                return RecognizedTextBox(
                    text: top.string,
                    boundingBox: $0.boundingBox
                )
            }

            Task { await self.handleRecognizedTexts(newTexts) }
        }

        request.recognitionLanguages = ["zh-Hans", "zh-Hant"]
        request.recognitionLevel = .accurate
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try? handler.perform([request])
    }

    func checkPermissionsAndStart() async {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            await startSessionOnBackgroundThread()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted { await startSessionOnBackgroundThread() }
        case .denied, .restricted:
            print(
                "⚠️ Camera permission denied. Enable them on Settings > Privacy > Camera"
            )
        @unknown default:
            break
        }
    }

    private func startSessionOnBackgroundThread() async {
        // If you had heavy pre-work, do it off the main actor:
        // await Task.detached { /* heavy work */ }.value
        // Then hop to main actor to touch AVCaptureSession.
        await MainActor.run {
            self.configureSession()
        }
    }

    private func configureSession() {
        session.beginConfiguration()
        guard let device = AVCaptureDevice.default(for: .video),
            let input = try? AVCaptureDeviceInput(device: device),
            session.canAddInput(input),
            session.canAddOutput(output)
        else {
            session.commitConfiguration()
            return
        }

        session.addInput(input)
        session.addOutput(output)
        session.commitConfiguration()
        session.startRunning()
    }

    private func handleRecognizedTexts(_ texts: [RecognizedTextBox]) async {
        recognizedTexts = texts
        for text in texts {
            let hanzi = HanziExtractor().extract(text: text.text)
            if hanzi == nil {
                continue
            }
            pinyinMap[text.id] = PinyinConverter().hanziToPinyin(
                hanzi: hanzi!
            )
        }

    }
}
