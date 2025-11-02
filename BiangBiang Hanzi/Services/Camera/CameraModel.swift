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
class CameraModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate,
    AVCaptureVideoDataOutputSampleBufferDelegate
{
    @Published var recognizedTexts: [RecognizedTextBox] = []
    @Published var pinyinMap: [UUID: String] = [:]

    let session = AVCaptureSession()
    private let videoOutput = AVCaptureVideoDataOutput()
    private let output = AVCapturePhotoOutput()
    private let queue = DispatchQueue(label: "camera.frame.processing")
    private var textRequest: VNRecognizeTextRequest!
    private var lastProcessingTime = Date.distantPast

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

    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        let now = Date()
        guard now.timeIntervalSince(lastProcessingTime) > 1 else { return }
        lastProcessingTime = now

        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
        else { return }

        let orientation: CGImagePropertyOrientation = .up

        let request = VNRecognizeTextRequest { [weak self] req, _ in
            guard let self,
                let results = req.results as? [VNRecognizedTextObservation]
            else { return }

            let boxes: [RecognizedTextBox] = results.compactMap {
                guard let top = $0.topCandidates(1).first else { return nil }
                return RecognizedTextBox(
                    text: top.string,
                    boundingBox: $0.boundingBox
                )
            }

            // Aggiorna UI sul main, identico alla tua funzione che già funziona
            DispatchQueue.main.async {
                self.recognizedTexts = boxes
                self.pinyinMap.removeAll(keepingCapacity: true)
                for box in boxes {
                    if let hanzi = HanziExtractor().extract(text: box.text) {
                        self.pinyinMap[box.id] = PinyinConverter()
                            .hanziToPinyin(hanzi: hanzi)
                    }
                }
            }
        }
        request.recognitionLanguages = ["zh-Hans", "zh-Hant"]
        request.recognitionLevel = .accurate

        // Handler per questo frame
        let handler = VNImageRequestHandler(
            cvPixelBuffer: pixelBuffer,
            orientation: orientation,
            options: [:]
        )

        // Esegui subito sulla queue del delegate (è già seriale), niente hop extra
        do {
            try handler.perform([request])
        } catch {
            print("⚠️ Vision error:", error)
        }

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
            await configureAndStartSession()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted {
                await configureAndStartSession()
            }
        case .denied, .restricted:
            print(
                "⚠️ Camera permission denied. Enable them on Settings > Privacy > Camera"
            )
        @unknown default:
            break
        }
    }

    private func configureAndStartSession() async {
        // Configure on main actor
        configureSession()
        // Start running on a background thread
        await startCaptureSession()
    }

    private func configureSession() {
        session.beginConfiguration()
        defer { session.commitConfiguration() }

        guard let device = AVCaptureDevice.default(for: .video) else { return }

        // Remove existing inputs to avoid duplicates
        for input in session.inputs {
            session.removeInput(input)
        }

        guard let input = try? AVCaptureDeviceInput(device: device) else {
            return
        }

        if session.canAddInput(input) {
            session.addInput(input)
        }

        if session.canAddOutput(output) {
            session.addOutput(output)
        }
        if session.canAddOutput(videoOutput) {
            videoOutput.setSampleBufferDelegate(self, queue: queue)
            session.addOutput(videoOutput)
        }
    }

    // Start the session off the main thread
    private func startCaptureSession() async {
        await withCheckedContinuation { continuation in
            Task.detached { [weak self] in
                guard let self else {
                    continuation.resume()
                    return
                }
                if await !self.session.isRunning {
                    await self.session.startRunning()
                }
                continuation.resume()
            }
        }
    }

    // Optional: stop session off the main thread when needed
    func stopSession() async {
        await withCheckedContinuation { continuation in
            Task.detached { [weak self] in
                guard let self else {
                    continuation.resume()
                    return
                }
                if await self.session.isRunning {
                    await self.session.stopRunning()
                }
                continuation.resume()
            }
        }
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

    private func handleTextRecognition(request: VNRequest, error: Error?) {
        guard let results = request.results as? [VNRecognizedTextObservation]
        else { return }

        DispatchQueue.main.async {
            self.recognizedTexts = results.compactMap { obs in
                guard let top = obs.topCandidates(1).first else { return nil }
                return RecognizedTextBox(
                    text: top.string,
                    boundingBox: obs.boundingBox
                )
            }

            // Conversione a pinyin
            for box in self.recognizedTexts {
                if let text = results.first(where: {
                    $0.boundingBox == box.boundingBox
                })?.topCandidates(1).first?.string {
                    // Extract only Hanzi, then convert to Pinyin
                    if let hanzi = HanziExtractor().extract(text: text) {
                        self.pinyinMap[box.id] = PinyinConverter()
                            .hanziToPinyin(hanzi: hanzi)
                    } else {
                        self.pinyinMap[box.id] = ""
                    }
                }
            }
        }
    }

}
