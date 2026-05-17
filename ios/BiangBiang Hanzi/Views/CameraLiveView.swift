//
//  CameraLiveView.swift
//  BiangBiang Hanzi
//
//  Main camera capture surface with live OCR overlays and controls.
//

import PhotosUI
import SwiftUI

struct CameraLiveView: View {
    @Bindable var cameraModel: CameraModel
    @State private var selectedItem: PhotosPickerItem?
    @State private var pinchBaseZoom: CGFloat = 1.0

    var body: some View {
        GeometryReader { geo in
            ZStack {
                CameraPreview(
                    session: cameraModel.session,
                    cameraModel: cameraModel
                )
                .ignoresSafeArea()

                if let image = cameraModel.capturedImage {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFit()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black)
                        .ignoresSafeArea()
                }

                ForEach(cameraModel.recognizedTexts) { box in
                    if let pinyin = cameraModel.pinyinMap[box.id] {
                        RecognizedTextOverlay(
                            cameraModel: cameraModel,
                            hanzi: box.text,
                            pinyin: pinyin,
                            boundingBox: box,
                            viewSize: geo.size
                        )
                    }
                }

                if !cameraModel.recognizedTexts.isEmpty {
                    VStack {
                        Text("Tap to copy · long-press to save")
                            .font(.caption2)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(.ultraThinMaterial, in: Capsule())
                            .padding(.top, 8)
                        Spacer()
                    }
                    .allowsHitTesting(false)
                }

                VStack {
                    Spacer()
                    if cameraModel.capturedImage == nil
                        && !cameraModel.availableZoomPresets.isEmpty
                    {
                        zoomPresetBar
                    }
                    controlBar
                }

                if cameraModel.showCopiedToast {
                    VStack {
                        Spacer()
                        CopyToast()
                            .padding(.bottom, AppDesign.bottomToolbarPadding)
                    }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .allowsHitTesting(false)
                }

                if cameraModel.showSavedToast {
                    VStack {
                        Spacer()
                        CopyToast(message: "Saved to History")
                            .padding(.bottom, AppDesign.bottomToolbarPadding + 44)
                    }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .allowsHitTesting(false)
                }
            }
            .animation(
                .easeOut(duration: AppDesign.toastAnimation),
                value: cameraModel.showCopiedToast
            )
            .animation(
                .easeOut(duration: AppDesign.toastAnimation),
                value: cameraModel.showSavedToast
            )
        }
        .simultaneousGesture(
            MagnifyGesture()
                .onChanged { value in
                    cameraModel.setZoom(pinchBaseZoom * value.magnification)
                }
                .onEnded { _ in
                    pinchBaseZoom = cameraModel.zoomFactor
                }
        )
        .task {
            await cameraModel.checkPermissionsAndStart()
        }
    }

    private var zoomPresetBar: some View {
        HStack(spacing: AppDesign.stackSpacing) {
            ForEach(cameraModel.availableZoomPresets, id: \.self) { preset in
                let isActive = abs(cameraModel.zoomFactor - preset) < 0.05
                Button {
                    cameraModel.setZoom(preset)
                    pinchBaseZoom = preset
                } label: {
                    Text("\(Int(preset))x")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.white)
                        .frame(
                            width: AppDesign.tapTarget,
                            height: AppDesign.tapTarget
                        )
                        .background(
                            Circle()
                                .fill(
                                    isActive
                                        ? Color.red.opacity(0.85)
                                        : Color.gray.opacity(0.6)
                                )
                                .shadow(radius: 3)
                        )
                }
                .accessibilityLabel("Zoom \(Int(preset))x")
            }
        }
        .padding(.bottom, AppDesign.stackSpacing)
    }

    private var controlBar: some View {
        HStack {
            if cameraModel.capturedImage != nil {
                CircularIconButton(
                    title: "Retake photo",
                    systemImage: "xmark.circle.fill",
                    action: cameraModel.deleteCapturedImage
                )
            } else {
                togglePinyinButton
                CircularIconButton(
                    title: "Take photo",
                    systemImage: "camera.fill",
                    action: cameraModel.capturePhoto
                )
                galleryPickerButton
            }
        }
        .padding(.bottom, AppDesign.bottomToolbarPadding)
    }

    private var togglePinyinButton: some View {
        Button {
            cameraModel.showPinyin.toggle()
        } label: {
            Image("BiangBiang")
                .resizable()
                .scaledToFit()
                .frame(width: 28, height: 28)
                .foregroundStyle(.white)
                .padding()
                .background(
                    Circle()
                        .fill(
                            cameraModel.showPinyin
                                ? Color.red.opacity(0.8)
                                : Color.gray.opacity(0.8)
                        )
                        .shadow(radius: AppDesign.buttonShadow)
                )
        }
        .animation(
            .easeInOut(duration: AppDesign.shortAnimation),
            value: cameraModel.showPinyin
        )
        .accessibilityLabel("Toggle Pinyin")
    }

    private var galleryPickerButton: some View {
        PhotosPicker(
            selection: $selectedItem,
            matching: .images,
            photoLibrary: .shared()
        ) {
            Image(systemName: "photo.on.rectangle")
                .font(.system(size: 24))
                .foregroundStyle(.white)
                .padding()
                .background(
                    Circle()
                        .fill(.gray)
                        .shadow(radius: AppDesign.buttonShadow)
                )
        }
        .accessibilityLabel("Scan photo from gallery")
        .onChange(of: selectedItem) { _, newItem in
            guard let newItem else { return }
            Task {
                if let data = try? await newItem.loadTransferable(type: Data.self),
                   let image = UIImage(data: data)
                {
                    cameraModel.recognizeGalleryImage(image)
                }
                selectedItem = nil
            }
        }
    }
}

private struct CircularIconButton: View {
    let title: String
    let systemImage: String
    let action: () -> Void

    var body: some View {
        Button(title, systemImage: systemImage, action: action)
            .labelStyle(.iconOnly)
            .font(.system(size: 24))
            .foregroundStyle(.white)
            .padding()
            .background(
                Circle()
                    .fill(.gray)
                    .shadow(radius: AppDesign.buttonShadow)
            )
    }
}
