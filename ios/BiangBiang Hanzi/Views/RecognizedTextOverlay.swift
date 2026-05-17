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
    @Environment(AppSettings.self) private var settings
    @State private var isCopied = false
    @State private var measuredSize: CGSize = .zero

    let cameraModel: CameraModel
    let hanzi: String
    let pinyin: String
    let boundingBox: RecognizedTextBox
    let viewSize: CGSize

    private static let minFontSize: CGFloat = 12
    private static let minTapTarget: CGFloat = 44
    private static let edgeInset: CGFloat = 4
    private static let pillGap: CGFloat = 2

    var body: some View {
        let frame = visionToViewRect(boundingBox.boundingBox, in: viewSize)
        let textToDisplay = cameraModel.showPinyin ? pinyin : hanzi
        let scaleRatio =
            cameraModel.showPinyin
                ? CGFloat(hanzi.count) / CGFloat(max(pinyin.count, 1)) : 1.0
        let scaleFactor = min(max(scaleRatio, 0.6), 1.0)
        let fontSize = max(Self.minFontSize, frame.height * scaleFactor)

        let estimatedHeight = max(Self.minTapTarget, fontSize + 12)
        let pillHeight =
            measuredSize.height > 0 ? measuredSize.height : estimatedHeight
        let pillWidth =
            measuredSize.width > 0
                ? measuredSize.width
                : max(Self.minTapTarget, frame.width)

        let desiredX = frame.midX
        let desiredY = frame.minY - pillHeight * 0.5 - Self.pillGap
        let position = clampPosition(
            CGPoint(x: desiredX, y: desiredY),
            size: CGSize(width: pillWidth, height: pillHeight),
            in: viewSize
        )

        Button {
            copy(textToDisplay)
        } label: {
            HStack(spacing: 4) {
                Text(textToDisplay)
                    .font(.system(size: fontSize, weight: .medium))
                    .foregroundStyle(.primary)
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
                if isCopied {
                    Image(systemName: "checkmark")
                        .font(.system(size: fontSize * 0.7, weight: .bold))
                        .foregroundStyle(Color.accentColor)
                        .transition(.scale.combined(with: .opacity))
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(
                RoundedRectangle(cornerRadius: AppDesign.cornerRadiusCompact)
                    .fill(.regularMaterial)
                    .shadow(
                        color: .black.opacity(0.25),
                        radius: 2,
                        x: 0,
                        y: 1
                    )
            )
            .scaleEffect(isCopied ? 1.08 : 1.0)
            .background(sizeReader)
        }
        .buttonStyle(.plain)
        .simultaneousGesture(
            LongPressGesture(minimumDuration: 0.3).onEnded { _ in
                let variant: HistoryVariant =
                    settings.isCantonese ? .cantonese : .mandarin
                settings.addHistory(
                    original: hanzi,
                    transliteration: pinyin,
                    variant: variant
                )
                cameraModel.showSavedToast = true
                Task {
                    try? await Task.sleep(for: .seconds(1.5))
                    cameraModel.showSavedToast = false
                }
            }
        )
        .frame(
            minWidth: Self.minTapTarget,
            minHeight: Self.minTapTarget
        )
        .contentShape(Rectangle())
        .position(position)
        .animation(
            .easeOut(duration: AppDesign.shortAnimation),
            value: isCopied
        )
        .sensoryFeedback(.impact(weight: .light), trigger: isCopied) { _, new in
            new
        }
        .accessibilityLabel(Text(textToDisplay))
        .accessibilityHint("Tap to copy, long-press to save to History")
    }

    private var sizeReader: some View {
        GeometryReader { proxy in
            Color.clear
                .preference(key: SizePreferenceKey.self, value: proxy.size)
        }
        .onPreferenceChange(SizePreferenceKey.self) { newSize in
            Task { @MainActor in
                if newSize != measuredSize {
                    measuredSize = newSize
                }
            }
        }
    }

    private func clampPosition(
        _ point: CGPoint,
        size: CGSize,
        in container: CGSize
    ) -> CGPoint {
        guard size.width > 0, size.height > 0,
              container.width > size.width,
              container.height > size.height
        else { return point }
        let halfW = size.width * 0.5
        let halfH = size.height * 0.5
        let x = min(
            max(point.x, halfW + Self.edgeInset),
            container.width - halfW - Self.edgeInset
        )
        let y = min(
            max(point.y, halfH + Self.edgeInset),
            container.height - halfH - Self.edgeInset
        )
        return CGPoint(x: x, y: y)
    }

    private func copy(_ text: String) {
        UIPasteboard.general.string = text

        isCopied = true
        cameraModel.showCopiedToast = true
        Task {
            try? await Task.sleep(for: .milliseconds(600))
            withAnimation(.easeOut(duration: AppDesign.shortAnimation)) {
                isCopied = false
            }
            try? await Task.sleep(for: .milliseconds(400))
            withAnimation(.easeOut(duration: AppDesign.shortAnimation)) {
                cameraModel.showCopiedToast = false
            }
        }
    }

    /// Convert Vision-normalized rect (image space, bottom-left origin) to view rect.
    /// X is mirrored relative to `layerRectConverted(fromMetadataOutputRect:)` because the
    /// preview connection runs at `videoRotationAngle = 0` (sensor-landscape) while Vision
    /// returns coords in sensor-up space — for the back camera in portrait, that mismatch
    /// flips the x-axis. TODO: drive Vision off the same orientation as the preview connection
    /// so this manual flip is no longer needed.
    private func visionToViewRect(_ rect: CGRect, in size: CGSize) -> CGRect {
        // Captured/gallery image: map against the aspect-fit rect of the displayed image,
        // not the preview layer (whose aspect ratio belongs to the live camera sensor).
        if let image = cameraModel.capturedImage {
            return imageRect(rect, image: image, in: size)
        }

        if let previewLayer = cameraModel.previewLayer {
            let videoRect = previewLayer.layerRectConverted(
                fromMetadataOutputRect: rect
            )
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

    /// Map Vision's normalized rect (bottom-left origin) onto the displayed
    /// `scaledToFit` rect of `image` inside `viewSize`.
    private func imageRect(
        _ rect: CGRect,
        image: UIImage,
        in viewSize: CGSize
    ) -> CGRect {
        let imageSize = image.size
        guard imageSize.width > 0, imageSize.height > 0,
              viewSize.width > 0, viewSize.height > 0
        else { return .zero }

        let scale = min(
            viewSize.width / imageSize.width,
            viewSize.height / imageSize.height
        )
        let displayW = imageSize.width * scale
        let displayH = imageSize.height * scale
        let offsetX = (viewSize.width - displayW) * 0.5
        let offsetY = (viewSize.height - displayH) * 0.5

        let x = offsetX + rect.minX * displayW
        let y = offsetY + (1 - rect.maxY) * displayH
        let width = rect.width * displayW
        let height = rect.height * displayH
        return CGRect(x: x, y: y, width: width, height: height)
    }
}

private struct SizePreferenceKey: PreferenceKey {
    static let defaultValue: CGSize = .zero

    static func reduce(value: inout CGSize, nextValue: () -> CGSize) {
        value = nextValue()
    }
}
