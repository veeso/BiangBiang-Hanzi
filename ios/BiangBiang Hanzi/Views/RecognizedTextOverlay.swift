//
//  RecognizedTextOverlay.swift
//  BiangBiang Hanzi
//
//  Tappable overlay rendered on top of recognized Hanzi or its pinyin.
//

import AVFoundation
import SwiftUI
import UIKit

struct RecognizedTextOverlay: View {
    @State private var isCopied = false

    let cameraModel: CameraModel
    let hanzi: String
    let pinyin: String
    let boundingBox: RecognizedTextBox
    let viewSize: CGSize

    var body: some View {
        let frame = visionToViewRect(boundingBox.boundingBox, in: viewSize)
        let textToDisplay = cameraModel.showPinyin ? pinyin : hanzi
        let scaleRatio =
            cameraModel.showPinyin
                ? CGFloat(hanzi.count) / CGFloat(max(pinyin.count, 1)) : 1.0
        let scaleFactor = min(max(scaleRatio, 0.6), 1.0)
        let fontSize = max(8, frame.height * scaleFactor)
        let x = max(0, frame.minX + (frame.width * 0.5))
        let y = max(0, frame.minY - (frame.height * 0.5) - (fontSize * 0.2))

        Button {
            copy(textToDisplay)
        } label: {
            Text(textToDisplay)
                .font(.system(size: fontSize, weight: .medium))
                .foregroundStyle(isCopied ? Color.blue : Color.black)
                .minimumScaleFactor(0.5)
                .lineLimit(1)
                .padding(4)
                .background(
                    RoundedRectangle(cornerRadius: AppDesign.cornerRadiusCompact)
                        .fill(Color.white.opacity(0.9))
                        .shadow(
                            color: .black.opacity(0.2),
                            radius: 1,
                            x: 0,
                            y: 1
                        )
                )
        }
        .buttonStyle(.plain)
        .frame(width: frame.width, height: frame.height)
        .position(x: x, y: y)
        .animation(.easeOut(duration: AppDesign.toastAnimation), value: isCopied)
        .sensoryFeedback(.impact(weight: .light), trigger: isCopied) { _, new in
            new
        }
        .accessibilityLabel(Text(textToDisplay))
        .accessibilityHint("Copy to clipboard")
    }

    private func copy(_ text: String) {
        UIPasteboard.general.string = text

        isCopied = true
        cameraModel.showCopiedToast = true
        Task {
            try? await Task.sleep(for: .milliseconds(250))
            isCopied = false
            try? await Task.sleep(for: .milliseconds(750))
            withAnimation(.easeOut(duration: AppDesign.shortAnimation)) {
                cameraModel.showCopiedToast = false
            }
        }
    }

    private func visionToViewRect(_ rect: CGRect, in size: CGSize) -> CGRect {
        if let previewLayer = cameraModel.previewLayer {
            let videoRect = previewLayer.layerRectConverted(
                fromMetadataOutputRect: rect
            )
            // X must be flipped for some reason in the metadata output coords.
            let flippedX = size.width - videoRect.origin.x - videoRect.width
            return CGRect(
                x: flippedX,
                y: videoRect.origin.y,
                width: videoRect.width,
                height: videoRect.height
            )
        }

        // Fallback when preview layer is unavailable.
        let x = rect.minX * size.width
        let y = rect.midY * size.height
        let width = rect.width * size.width
        let height = rect.height * size.height
        return CGRect(x: x, y: y, width: width, height: height)
    }
}
