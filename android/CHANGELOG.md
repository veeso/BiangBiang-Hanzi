# Changelog

All notable changes to this project will be documented in this file.

- [Changelog](#changelog)
  - [0.5.0](#050)
  - [0.4.0](#040)
  - [0.3.1](#031)
  - [0.3.0](#030)
  - [0.2.0](#020)
  - [0.1.1](#011)

## 0.5.0

Released on 18/05/2026

- Added audio playback for Hanzi text:
  - New `AudioPlayerService` wrapping the platform `TextToSpeech` engine with Mandarin/Cantonese voice selection wired to the Chinese variant setting; async engine init queues a pending utterance and the service is shut down in `onCleared()`.
  - Listen/Stop control in text mode.
- Added a History feature: a new History tab listing explicitly-saved Hanzi entries, persisted via DataStore.
- Added a post-launch in-app review prompt: after 5 cold launches the Play In-App Review flow is requested once at app open, with a completion-time heuristic to detect a silent no-op, attempt cap and retry, plus DEBUG state readout and reset controls.
- Fixed OCR overlay rendering:
  - Uniform aspect-fill (live) / aspect-fit (captured) scaling with centered offsets so overlay boxes align with on-screen text.
  - Swap image dimensions for 90°/270° rotation to stop horizontal drift and wrong font scale on the live path.
  - Reduced overlay font multiplier (0.5 → 0.4) with a 12sp floor and filtered spurious oversized detections.
- Migrated the entire UI layer to the shared **BiangBiangUI** library, replacing the local Compose screens, camera/OCR, `TextProcessor`, history, settings, theme and review-prompt code with config and transliterator wrappers.
- Upgraded the build toolchain: AGP 9.2.1, Gradle 9.4.1, Kotlin 2.3.21, Compose BOM and AndroidX deps; migrated to AGP 9 built-in Kotlin and the `kotlin.compilerOptions` DSL.
- Added CI: `gradlew test`, `lintDebug` and `assembleDebug` on JDK 21.

## 0.4.0

Released on 09/05/2026

- Added Cantonese support:
  - New `JyutpingDictionary` backed by a bundled `cantonese.json` generated from the Unihan `kCantonese` field.
  - `TextProcessor` gains a `CANTONESE` mode that romanizes Hanzi to Jyutping.
  - Settings expose a "Cantonese" Chinese variant; text mode swaps the "Pinyin" header for "Jyutping" and hides the translation section since ML Kit does not translate Cantonese.
- Settings UI: replaced the Chinese variant `FilterChip` row with a `SingleChoiceSegmentedButtonRow` so the Cantonese option fits on narrow screens, mirroring the iOS segmented picker.
- Restored the fullwidth comma (`，`) in the `TextProcessor` punctuation set so it is preserved instead of stripped from converted output.

## 0.3.1

Released on 06/05/2026

- Modernized the Compose UI to mirror the iOS refactor:
  - Replaced the `Int` tab constants in `MainScreen` with a `Tab` enum and `rememberSaveable` selection.
  - Wired `TextModeView` through the `viewModel()` factory instead of constructing `TextModeViewModel` on every recomposition.
  - Added a coroutine-based debounce (`viewModelScope` + `Job` + `delay`) to text input handling.
- Camera view polish:
  - Split `CameraModeView` into a permission orchestrator plus a dedicated `CameraPermissionView` (with a deep link to the app's system settings) and a `CameraLiveView` containing the preview, OCR overlay and controls.
  - Extracted private `ZoomPresetBar` and `CaptureControlBar` composables.
  - Replaced the native `Toast` shown when copying recognized text with an in-app `CopyToast` overlay and added haptic feedback.
- Extracted reusable `SectionView` and `CopyToast` components and added a shared `AppDesign` object with spacing, corner radii, animation durations, tap target and brand color constants.

## 0.3.0

Released on 02/05/2026

- [Issue 13](https://github.com/veeso/BiangBiang-Hanzi/issues/13): Camera controls. Pinch-to-zoom and 1x/2x/5x preset buttons in the camera view.

## 0.2.0

Released on 03/01/2026

- [Issue 10](https://github.com/veeso/BiangBiang-Hanzi/issues/10): Do not strip non hanzi characters from sentence that contains hanzi. For instance `我在NASA工作. 现在是5点.` caused converted text to be just `wǒ zài gōng zuò. xiàn zài shì diǎn.` instead of `wǒ zài NASA gōng zuò. xiàn zài shì 5 diǎn.`.
- [Issue 9](https://github.com/veeso/BiangBiang-Hanzi/issues/9): Added tab to file issues from within the app.

## 0.1.1

Released on 08/11/2025

- First stable release of the Android application.
