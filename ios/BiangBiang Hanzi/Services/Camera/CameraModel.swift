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
    let boundingBox: CGRect // Normalized (0-1)
}

@MainActor
class CameraModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate,
    AVCaptureVideoDataOutputSampleBufferDelegate
{
    /// Recognised text by camera
    @Published var recognizedTexts: [RecognizedTextBox] = []
    /// Map between the recognised text id and the pinyin for it
    @Published var pinyinMap: [UUID: String] = [:]
    /// If the user captured an image, it will be saved here
    @Published var capturedImage: UIImage? = nil
    /// No camera permission
    @Published var missingCameraPermission: Bool = false
    /// Whether to show pinyin instead of Hanzi in overlays
    @Published var showPinyin: Bool = true
    /// Show copied toast
    @Published var showCopiedToast: Bool = false

    /// Currently active capture device. Used for zoom configuration.
    private var device: AVCaptureDevice?
    /// UI-facing zoom factor (1.0 == standard wide lens).
    @Published var zoomFactor: CGFloat = 1.0
    /// Presets available on the active device.
    @Published var availableZoomPresets: [CGFloat] = []
    /// Switch-over factor mapping UI zoom → device.videoZoomFactor (1.0 if no virtual switch).
    private var zoomSwitchOverFactor: CGFloat = 1.0
    /// Maximum UI zoom supported by the active device.
    private var maxUIZoom: CGFloat = 1.0

    let session = AVCaptureSession()
    private let videoOutput = AVCaptureVideoDataOutput()
    private let output = AVCapturePhotoOutput()
    private let queue = DispatchQueue(label: "camera.frame.processing")
    private var textRequest: VNRecognizeTextRequest!
    var previewLayer: AVCaptureVideoPreviewLayer?
    private var lastProcessingTime = Date.distantPast
    private let textProcessor = TextProcessor()

    /// Capture a photo and start task to recognize text
    func capturePhoto() {
        output.capturePhoto(with: AVCapturePhotoSettings(), delegate: self)
    }

    /// Handle photo output
    func photoOutput(
        _: AVCapturePhotoOutput,
        didFinishProcessingPhoto photo: AVCapturePhoto,
        error _: Error?
    ) {
        // called after taking photo
        guard let imageData = photo.fileDataRepresentation(),
              let image = UIImage(data: imageData)
        else { return }
        Task { @MainActor in
            // save image
            self.capturedImage = image
            // stop live feed
            // self.session.stopRunning()
            // get text from image
            self.recognizeText(from: image)
        }
    }

    /// Capture live camera output
    func captureOutput(
        _: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from _: AVCaptureConnection
    ) {
        // do not capture if image is set underneath.
        if capturedImage != nil { return }

        let now = Date()
        guard now.timeIntervalSince(lastProcessingTime) > 1 else { return }
        lastProcessingTime = now

        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
        else { return }

        let orientation: CGImagePropertyOrientation = .up

        let request = makeTextRecognitionRequest()

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

    /// Recognize and process text from a gallery image
    func recognizeGalleryImage(_ uiImage: UIImage) {
        // set image to gallery image
        capturedImage = uiImage
        // reck text
        recognizeText(from: uiImage)
    }

    /// Checks whether the user gave permission to camera.
    ///
    /// If given start the session.
    ///
    /// If not ask permission or return error.
    func checkPermissionsAndStart() async {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            await configureAndStartSession()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted {
                await configureAndStartSession()
            } else {
                await MainActor.run { self.missingCameraPermission = true }
            }
        case .denied, .restricted:
            print(
                "⚠️ Camera permission denied. Enable them on Settings > Privacy > Camera"
            )
            await MainActor.run { self.missingCameraPermission = true }
        @unknown default:
            await MainActor.run { self.missingCameraPermission = true }
        }
    }

    /// Delete the current captured image data.
    func deleteCapturedImage() {
        Task { @MainActor in
            // reset image
            self.capturedImage = nil
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

    private func configureAndStartSession() async {
        // Configure on main actor
        configureSession()
        // Start running on a background thread
        await startCaptureSession()
    }

    private func configureSession() {
        session.beginConfiguration()
        defer { session.commitConfiguration() }

        let virtualDiscovery = AVCaptureDevice.DiscoverySession(
            deviceTypes: [
                .builtInTripleCamera,
                .builtInDualWideCamera,
                .builtInDualCamera,
                .builtInWideAngleCamera,
            ],
            mediaType: .video,
            position: .back
        )

        let device =
            virtualDiscovery.devices.first(where: { $0.deviceType == .builtInTripleCamera })
                ?? virtualDiscovery.devices.first(where: { $0.deviceType == .builtInDualWideCamera })
                ?? virtualDiscovery.devices.first(where: { $0.deviceType == .builtInDualCamera })
                ?? virtualDiscovery.devices.first(where: { $0.deviceType == .builtInWideAngleCamera })

        guard let device else { return }
        self.device = device

        // configure camera focus
        do {
            try device.lockForConfiguration()

            if let format = device.formats.first(where: {
                $0.videoSupportedFrameRateRanges.first!.maxFrameRate >= 30
            }) {
                device.activeFormat = format
            }

            if device.isFocusPointOfInterestSupported {
                device.focusPointOfInterest = CGPoint(x: 0.5, y: 0.5)
                device.focusMode = .autoFocus
            }

            // Continuous AF
            if device.isFocusModeSupported(.continuousAutoFocus) {
                device.focusMode = .continuousAutoFocus
            }

            // Exposure continuous
            if device.isExposureModeSupported(.continuousAutoExposure) {
                device.exposureMode = .continuousAutoExposure
            }

            let switchOvers = device.virtualDeviceSwitchOverVideoZoomFactors.map { CGFloat(truncating: $0) }
            let switchOver = switchOvers.first ?? 1.0
            zoomSwitchOverFactor = switchOver

            let maxDeviceZoom = device.maxAvailableVideoZoomFactor
            let maxUI = deviceZoomToUIZoom(maxDeviceZoom, switchOverFactor: switchOver)
            maxUIZoom = maxUI

            // Default to 1.0x (standard wide lens).
            let initialDeviceZoom = uiZoomToDeviceZoom(1.0, switchOverFactor: switchOver)
            device.videoZoomFactor = clampZoom(
                initialDeviceZoom,
                min: device.minAvailableVideoZoomFactor,
                max: maxDeviceZoom
            )

            device.unlockForConfiguration()

            zoomFactor = 1.0
            availableZoomPresets = availablePresets(maxUIZoom: maxUIZoom)
        } catch {
            print("⚠️ Failed to configure camera focus: \(error)")
        }

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

    /// Start the session off the main thread
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

    /// Make the text recognition request for OCR
    private func makeTextRecognitionRequest() -> VNRecognizeTextRequest {
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
                    if let pinyin = self.textProcessor.process(text: box.text) {
                        self.pinyinMap[box.id] = pinyin
                    }
                }
            }
        }
        request.recognitionLanguages = ["zh-Hans", "zh-Hant"]
        request.recognitionLevel = .accurate

        return request
    }

    /// Recognize the text from an image
    private func recognizeText(from image: UIImage) {
        guard let cgImage = image.cgImage else { return }
        let request = makeTextRecognitionRequest()
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try? handler.perform([request])
    }

    /// Function which processed the recognized texts to extract hanzi and get the pinyin.
    private func handleRecognizedTexts(_ texts: [RecognizedTextBox]) async {
        recognizedTexts = texts
        for text in texts {
            if let pinyin = textProcessor.process(text: text.text) {
                pinyinMap[text.id] = pinyin
            }
        }
    }

    /// Set the UI zoom factor. Clamps to the active device's range.
    func setZoom(_ uiZoom: CGFloat) {
        guard let device else { return }
        let clampedUI = clampZoom(uiZoom, min: 1.0, max: maxUIZoom)
        let deviceZoom = uiZoomToDeviceZoom(clampedUI, switchOverFactor: zoomSwitchOverFactor)
        let clampedDevice = clampZoom(
            deviceZoom,
            min: device.minAvailableVideoZoomFactor,
            max: device.maxAvailableVideoZoomFactor
        )
        do {
            try device.lockForConfiguration()
            device.videoZoomFactor = clampedDevice
            device.unlockForConfiguration()
            zoomFactor = clampedUI
        } catch {
            print("⚠️ Failed to set zoom: \(error)")
        }
    }
}
