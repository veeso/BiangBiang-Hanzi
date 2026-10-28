# Changelog

All notable changes to this project are documented in this file.

## 0.5.3

Released on 2026-10-28

### Fixed

- **android:** bump BiangBiangUI to 0.5.0 for camera wide layout and log

> BiangBiangUI 0.5 adds a wide split layout to the camera screen on wide
> built-in displays, with a session log pane next to the preview. The log
> keeps a bounded history of recognised text and preserves the physical
> pane order and the existing camera controls.
>
> Release the app as 0.5.3 (versionCode 53).

### Build

- **android:** bump BiangBiangUI to 0.5.1

## 0.5.2

Released on 2026-08-24

### Fixed

- **android:** add translation support for Cantonese using zh-CN. Works fine as a workaround until Android supports Cantonese translations natively

## 0.5.1

Released on 2026-05-18

### Added

- **android:** add audio playback for Hanzi text

> Add AudioPlayerService wrapping the platform TextToSpeech engine with
> Mandarin/Cantonese voice selection wired to the Chinese variant
> setting. TextModeViewModel owns the service and shuts it down in
> onCleared(); async engine init queues a pending utterance. Add a
> Listen/Stop control to text mode. Bump version to 0.5.0 (build 50).

- **android:** add History feature
- **android:** add post-launch in-app review prompt

### Changed

- **android:** migrate to BiangBiangUI

> Replace local Compose screens, camera/OCR, TextProcessor, history,
> settings, theme and review-prompt code with the shared BiangBiangUI
> library. Add config and PinyinTransliterator/JyutpingTransliterator
> wrappers. Bump BiangBiangUI to 0.1.3.

### Fixed

- **android:** correct OCR overlay scaling, font size, and spurious boxes

> - Use uniform aspect-fill (live) / aspect-fit (captured) scaling with
>   centered offsets so overlay boxes align with on-screen text, replacing
>   non-uniform scaleX/scaleY and the single-axis offset hack.
> - Swap image dims for 90/270 rotation: ML Kit returns boxes in upright
>   display space while image.width/height are unrotated buffer dims,
>   causing horizontal drift and wrong font scale on the live path.
> - Reduce overlay font multiplier (0.5 -> 0.4) and add a 12sp floor
>   matching iOS minFontSize.
> - Filter spurious oversized detections (>=50% frame height or >=90%
>   width) on both live and captured paths to stop random huge text.

### Build

- **deps:** bump BiangBiangUI to 0.2.0 on both apps

> Document in CLAUDE.md that the entire UI layer of both iOS and
> Android apps depends on the shared BiangBiangUI library.

## 0.4.0

Released on 2026-05-09

### Added

- **android:** add JyutpingDictionary with bundled cantonese.json
- **android:** add Cantonese mode to TextProcessor
- **android:** add Cantonese variant to settings and text mode UI

### Fixed

- **android:** restore fullwidth comma in TextProcessor punctuation set
- **android:** use segmented buttons for Chinese variant picker

> Replace FilterChip row with SingleChoiceSegmentedButtonRow so the
> Cantonese option is no longer cut off on narrow screens, matching the
> iOS segmented picker. Shorten labels to "Simplified" / "Traditional" to
> fit the segmented layout.

## 0.3.1

Released on 2026-05-06

### Changed

- **android:** modernize Compose UI to mirror iOS refactor

> Apply the same structural cleanups from the iOS modernization commit to
> the Android app for parity.
>
> State and lifecycle:
>
> - Replace Int tab constants in MainScreen with a Tab enum and
>   rememberSaveable selection.
> - Wire TextModeView through viewModel() instead of constructing the
>   TextModeViewModel on every recomposition.
> - Add a coroutine-based debounce (viewModelScope + Job + delay) to
>   TextModeViewModel input handling.

## 0.3.0

Released on 2026-05-02

### Added

- **android:** add pure CameraZoom helpers with tests
- **android:** pinch-to-zoom, preset buttons, single camera binding

### Changed

- **android:** capturePhoto takes LifecycleCameraController

### Fixed

- **android:** dispose camera zoom observer and import names
- **android:** use ScaleGestureDetector for pinch-to-zoom on PreviewView
- **android:** wrap PreviewView in FrameLayout to capture pinch gestures
- **android:** intercept all touches on preview container for pinch detector
- **android:** top-most pinch overlay above OcrOverlay
- **android:** chain pinch gesture into OcrOverlay modifier
- build number 30 for 0.3.0

## 0.2.0

Released on 2026-01-03

### Added

- **android:** Android app setup
- **android:** Hanzi and Pinyin services
- **android:** Settings and fragments setup
- **android:** Text Mode
- **android:** Camera View setup
- **android:** OCR
- **android:** Live OCR
- **android:** Copy OCR text on click
- **android:** Highlight selected box
- Added bug reports in settings view (#12)

### Fixed

- **android:** Live OCR sizes
- **android:** Proportional pinyin boxes
- **ios:** OCR text overlay position
- **android:** Do not strip non hanzi characters from sentence that contains hanzi.
