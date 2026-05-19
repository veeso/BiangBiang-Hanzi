# Changelog

All notable changes to this project will be documented in this file.

- [Changelog](#changelog)
  - [0.5.1](#051)
  - [0.5.0](#050)
  - [0.4.0](#040)
  - [0.3.1](#031)
  - [0.3.0](#030)
  - [0.2.0](#020)
  - [0.1.3](#013)
  - [0.1.2](#012)
  - [0.1.1](#011)

## 0.5.1

Released on 19/05/2026

- Updated app icon with iOS 26 style

## 0.5.0

Released on 18/05/2026

- Added audio playback for Hanzi text:
  - New `AudioPlayerService` built on `AVSpeechSynthesizer` with Mandarin/Cantonese voice selection wired to the Chinese variant setting.
  - Listen/Stop control in text mode.
- Added a History feature:
  - New History tab listing explicitly-saved Hanzi entries, backed by a pure `HistoryStore` (prepend, consecutive dedup by original + variant, silent 500-entry cap, delete, clear) persisted via `UserDefaults`.
  - Ephemeral Original/Transliterated toggle, expand/collapse rows, per-entry play/stop TTS, swipe-delete, Clear All and an empty state.
  - Save button in the Text tab and long-press on an OCR box (tap still copies), each with a confirmation toast.
- Added a rate-the-app prompt: a custom dialog shown on cold launch after 3+ launches that gates the App Store write-review flow, with cap and dismiss-forever logic.
- Migrated the entire UI layer to the shared **BiangBiangUI** Swift package, replacing the local SwiftUI views, camera, `TextProcessor`, history, settings and review-prompt code with `Config` and transliterator wrappers.
- Added CI: SwiftFormat lint plus `xcodebuild` build and test on macOS.

## 0.4.0

Released on 09/05/2026

- Added Cantonese support:
  - New `JyutpingDictionary` backed by a bundled `cantonese.json` generated from the Unihan `kCantonese` field.
  - `TextProcessor` gains a `.cantonese` mode that romanizes Hanzi to Jyutping.
  - Settings expose a "Cantonese" Chinese variant; text mode swaps the "Pinyin" header for "Jyutping", disables the translation language picker and hides the translation section since the Apple Translation API does not support Cantonese.
- Added a setting to dismiss the keyboard automatically when typing Hanzi on the home tab.
- Camera: configure the `AVCaptureSession` once at startup and only start/stop it on tab switch, removing the visible reconfiguration delay when returning to the camera tab.

## 0.3.1

Released on 06/05/2026

- Modernized the SwiftUI codebase to iOS 17+ patterns:
  - Migrated state from `ObservableObject` / `@Published` / `@StateObject` to `@Observable` + `@State` + `@Environment`.
  - Replaced `NavigationView` with `NavigationStack` and switched the tab bar to the iOS 17 `Tab` API with an enum-typed selection.
  - Replaced `MagnificationGesture`, `foregroundColor`, `cornerRadius` and `UIImpactFeedbackGenerator` with `MagnifyGesture`, `foregroundStyle`, `clipShape(.rect(...))` and `sensoryFeedback`.
  - Replaced Combine `debounce` and `DispatchQueue.asyncAfter` with cancellable `Task` + `Task.sleep`.
- Camera view polish:
  - Split into dedicated `CameraPermissionView`, `CameraLiveView`, `RecognizedTextOverlay` and `CopyToast` views.
  - Added a `ContentUnavailableView`-based permission screen with a deep link into Settings.
- Text view polish:
  - Replaced the read-only `TextEditor` with a `Text`-based `ReadOnlyTextBox` and switched the Hanzi input to `TextField(axis: .vertical)`.
- Added a shared `AppDesign` enum with spacing, corner radii, animation durations and tap target constants used across views.
- Fixed OCR overlays on gallery and captured photos rendering at the wrong size and position by mapping Vision's normalized box onto the displayed image's aspect-fit rect and passing the image orientation to the Vision request.

## 0.3.0

Released on 02/05/2026

- [Issue 13](https://github.com/veeso/BiangBiang-Hanzi/issues/13): Camera controls. Pinch-to-zoom and 1x/2x/5x preset buttons in the camera view, with automatic lens switching on devices with multiple back cameras (triple/dual).

## 0.2.0

Released on 03/01/2026

- [Issue 10](https://github.com/veeso/BiangBiang-Hanzi/issues/10): Do not strip non hanzi characters from sentence that contains hanzi. For instance `我在NASA工作. 现在是5点.` caused converted text to be just `wǒ zài gōng zuò. xiàn zài shì diǎn.` instead of `wǒ zài NASA gōng zuò. xiàn zài shì 5 diǎn.`.
- [Issue 9](https://github.com/veeso/BiangBiang-Hanzi/issues/9): Added tab to file issues from within the app.

## 0.1.3

Released on 16/11/2025

- [Issue 2](https://github.com/veeso/BiangBiang-Hanzi/issues/2): Fixed camera not auto-focusing when moving closer to text.
- [Issue 4](https://github.com/veeso/BiangBiang-Hanzi/issues/4): Better feedback for copying text to clipboard from text overlays.

## 0.1.2

Released on 07/11/2025

- Fixed OCR box position

## 0.1.1

Released on 05/11/2025

- First stable release of the iOS application.
