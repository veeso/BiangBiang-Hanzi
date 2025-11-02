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

@MainActor
class CameraModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    @Published var recognizedText: String = ""
    @Published var pinyinText: String = ""

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
        let request = VNRecognizeTextRequest { [weak self] request, _ in
            guard
                let observations = request.results
                    as? [VNRecognizedTextObservation]
            else { return }
            let text = observations.compactMap {
                $0.topCandidates(1).first?.string
            }.joined(separator: " ")
            Task { await self?.handleRecognizedText(text) }
        }
        request.recognitionLanguages = ["zh-Hans", "zh-Hant"]
        request.recognitionLevel = .accurate

        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try? handler.perform([request])
    }

    func checkPermissionsAndStart() async {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            configureSession()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted { configureSession() }
        case .denied, .restricted:
            print(
                "⚠️ Camera permission denied. Enable them on Settings > Privacy > Camera"
            )
        @unknown default:
            break
        }
    }

    private func configureSession() {
        session.beginConfiguration()
        guard let device = AVCaptureDevice.default(for: .video),
            let input = try? AVCaptureDeviceInput(device: device),
            session.canAddInput(input),
            session.canAddOutput(output)
        else { return }

        session.addInput(input)
        session.addOutput(output)
        session.commitConfiguration()
        session.startRunning()
    }

    private func handleRecognizedText(_ text: String) async {
        guard !text.isEmpty else { return }
        recognizedText = text
        pinyinText = PinyinConverter().hanziToPinyin(hanzi: text)
    }
}
