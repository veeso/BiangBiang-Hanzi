# Changelog

All notable changes to this project are documented in this file.

## 0.5.3

Released on 2026-10-28

### Fixed

- **ios:** bump BiangBiangUI to 0.5.0 for camera wide layout and log

> BiangBiangUI 0.5 adds a wide split layout to the camera screen on wide
> built-in displays, with a session log pane next to the preview. The log
> keeps a bounded history of recognised text and preserves the physical
> pane order and the existing camera controls. It also corrects the OCR
> overlay alignment in landscape.
>
> Release the app as 0.5.3 (build 53).

### Build

- **ios:** bump BiangBiangUI to 0.5.1

## 0.5.2

Released on 2026-08-24

### Fixed

- **ios:** add translation support for Cantonese using zh-CN. Works fine as a workaround until iOS supports Cantonese translations natively

## 0.5.1

Released on 2026-05-18

### Added

- **ios:** add audio playback for Hanzi text

> Add AudioPlayerService using AVSpeechSynthesizer with Mandarin/Cantonese
> voice selection wired to the Chinese variant setting. Add play/stop
> control to text mode. Bump version to 0.5.0 (build 50).

- **ios:** add History feature

> New History tab listing explicitly-saved Hanzi entries.
>
> - HistoryEntry model + pure HistoryStore (prepend, consecutive
>   dedup by original+variant, silent 500 cap, delete, clear)
> - Persist in AppSettings via UserDefaults (Codable JSON)
> - History tab: ephemeral Original/Transliterated toggle,
>   expand/collapse rows, per-entry play/stop TTS (speaks stored
>   Hanzi with stored variant voice), swipe-delete, Clear All,
>   empty state
> - Save button in Text tab with confirmation toast
> - Long-press OCR box saves (tap still copies) + caption and
>   "Saved to History" toast
> - Swift Testing: dedup, cap eviction, persistence round-trip

- **ios:** add rate-the-app popup after 3+ launches

> Custom 3-button dialog gating the App Store write-review flow,
> shown on cold launch when launchCount >= 3 and not dismissed.
>
> - ReviewPromptPolicy: pure show/cap decision logic + tests
> - AppSettings: reviewLaunchCount (cap 5), reviewPromptDismissed,
>   registerLaunch/notNow/dismissForever + persistence tests
> - App registers a launch once per process at init
> - ContentView presents the alert; Rate now opens the write-review
>   URL via openURL (reliable vs StoreKit requestReview)
> - DEBUG-only reset row in Settings with confirmation toast

### Changed

- **ios:** migrate to BiangBiangUI

> Replace local SwiftUI views, camera, TextProcessor, history, settings
> and review-prompt code with the shared BiangBiangUI Swift package.
> Add Config and PinyinTransliterator/JyutpingTransliterator wrappers
> plus transliterator parity tests. Bump BiangBiangUI to 0.1.3.

### Fixed

- **ios:** commit SwiftPM Package.resolved for Xcode Cloud

> Xcode Cloud disables automatic dependency resolution and requires a
> checked-in Package.resolved. The .gitignore '*.xcworkspace' rule
> excluded the whole workspace dir, so the file was never tracked and the
> Archive build failed resolving the 'biangbiangui' dependency.
>
> Re-include the swiftpm/Package.resolved path while keeping the rest of
> the workspace ignored.

### Build

- **deps:** bump BiangBiangUI to 0.2.0 on both apps

> Document in CLAUDE.md that the entire UI layer of both iOS and
> Android apps depends on the shared BiangBiangUI library.

## 0.4.0

Released on 2026-05-09

### Added

- **ios:** add JyutpingDictionary with bundled cantonese.json
- **ios:** add Cantonese mode to TextProcessor
- **ios:** add Cantonese variant to settings and text mode UI

### Fixed

- **ios:** configure capture session once, only start/stop on tab switch
- **ios:** add toggle for closing keyboard when typing hanzi in homepage

## 0.3.1

Released on 2026-05-06

### Changed

- **ios:** modernize SwiftUI to iOS 17+ patterns

> Migrate the iOS app interface to current SwiftUI APIs and fix design
> issues found during the SwiftUI Pro review.
>
> State and lifecycle:
>
> - Replace ObservableObject/@Published/@StateObject with @Observable +
>   @State + @Environment for AppSettings and CameraModel.
> - Drop deprecated NavigationView in favor of NavigationStack.
> - Switch TabView to the iOS 17 Tab API with an enum-typed selection.
>
> Views and modifiers:
>
> - Split CameraModeView into CameraPermissionView, CameraLiveView,
>   RecognizedTextOverlay and CopyToast files.
> - Extract SectionView into its own file.
> - Replace TextEditor read-only misuse with a Text-based ReadOnlyTextBox
>   and use TextField(axis: .vertical) for Hanzi input.
> - Use ContentUnavailableView for the camera permission screen with a
>   deep link into Settings.
> - Replace Combine debounce and DispatchQueue.asyncAfter with
>   cancellable Task + Task.sleep.
> - Replace MagnificationGesture with MagnifyGesture, foregroundColor
>   with foregroundStyle, cornerRadius with clipShape(.rect(...)),
>   UIImpactFeedbackGenerator with sensoryFeedback.
> - Use openURL environment instead of UIApplication.shared.open in
>   SettingsView.
> - Move animation modifiers to the outer view so the whole control
>   animates rather than just its background.

### Fixed

- **ios:** align OCR overlays on gallery and captured photos

> The recognized-text pill was sized and positioned using the camera
> preview layer's coordinate space, which does not match the displayed
> photo's aspect-fit rect — gallery images rendered overlays at huge,
> misplaced sizes. Map Vision's normalized box onto the displayed image
> rect when a captured image is present, and pass the UIImage's
> orientation to the Vision request so the box lives in upright space.

## 0.3.0

Released on 2026-05-02

### Added

- **ios:** add pure CameraZoom helpers with tests
- **ios:** switch CameraModel to virtual device + zoom state
- **ios:** pinch-to-zoom and 1x/2x/5x preset buttons in camera

### Fixed

- **ios:** preset button touch target 44pt, accessibilityLabel on Button
- **ios:** restore autoFocusRangeRestriction near for close-up text
- **ios:** re-apply initial zoom after session start
- **ios:** translate bug email button label to English
- build number 30 for 0.3.0

### Style

- format ios code

## 0.2.0

Released on 2026-01-03

### Added

- Added bug reports in settings view (#12)

### Changed

- ios folder

### Fixed

- **ios:** Ipad view
- **ios:** OCR text overlay position
- **ios:** Fixed camera not auto-focusing when moving closer to text. (#5)
- **ios:** Better feedback for copying text to clipboard from text overlays (#6)
- **ios:** 0.1.3
- **ios:** Do not strip non hanzi characters from sentence that contains hanzi

> For instance `我在NASA工作` caused converted text to be just `wǒ zài gōng zuò. xiàn zài shì diǎn.` instead of `wǒ zài NASA gōng zuò. xiàn zài shì 5 diǎn.`.

- **android:** Do not strip non hanzi characters from sentence that contains hanzi.
- typo
