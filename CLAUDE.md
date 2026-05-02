# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BiangBiang Hanzi is a dual-platform native mobile app (iOS + Android) that converts Chinese Hanzi characters to Pinyin and translates Chinese text. It supports OCR from images and live camera feed. The iOS and Android apps are **fully separate implementations** with no shared code — not a cross-platform framework.

## Build & Development Commands

### iOS (Swift + SwiftUI)

- **Open project:** `open "ios/BiangBiang Hanzi.xcodeproj"`
- **Build:** Xcode ⌘B or `xcodebuild -project "ios/BiangBiang Hanzi.xcodeproj" -scheme "BiangBiang Hanzi" build`
- **Run tests:** `xcodebuild test -project "ios/BiangBiang Hanzi.xcodeproj" -scheme "BiangBiang Hanzi" -destination 'platform=iOS Simulator,name=iPhone 16'`
- **Format code:** `swiftformat ./ios` (requires `brew install swiftformat`). **Run this whenever iOS code is modified.**

### Android (Kotlin + Jetpack Compose)

- **Build debug:** `cd android && ./gradlew assembleDebug`
- **Run unit tests:** `cd android && ./gradlew test`
- **Run a single test class:** `cd android && ./gradlew test --tests "dev.veeso.biangbianghanzi.TextProcessorTest"`
- **Run instrumented tests:** `cd android && ./gradlew connectedAndroidTest`

## Architecture

### iOS (`ios/BiangBiang Hanzi/`)

MVVM with SwiftUI + Combine. Entry point is `BiangBiang_HanziApp.swift` → `ContentView.swift` (TabView with Text, Camera, Settings tabs).

- **Services/TextProcessor.swift** — Hanzi→Pinyin conversion using `CFStringTransform`
- **Services/Camera/CameraModel.swift** — AVFoundation camera + Vision OCR (ObservableObject)
- **Views/** — SwiftUI views for each tab (TextModeView, CameraModeView, SettingsView)
- **AppSettings.swift** — User preferences via `UserDefaults` (ObservableObject, injected as `@EnvironmentObject`)

Uses native iOS Translation API for translation. Tests use Swift Testing (`@Test` syntax) in `ios/BiangBiang HanziTests/`.

### Android (`android/app/src/main/java/dev/veeso/biangbianghanzi/`)

MVVM with Jetpack Compose + ViewModel + DataStore. Entry point is `MainActivity.kt` → `MainScreen.kt` (bottom navigation).

- **services/TextProcessor.kt** — Hanzi→Pinyin conversion using `pinyin4j` library
- **services/OcrService.kt** — ML Kit Chinese text recognition + LiveOcrAnalyzer for camera
- **services/AppSettingsStore.kt** — DataStore Preferences for settings persistence
- **ui/screens/** — Compose screens (TextModeView, CameraModeView, SettingsModeView)
- **ui/screens/textmode/TextModeViewModel.kt** — ViewModel with StateFlow for text mode

Uses Google ML Kit Translate for translation. Dependency versions managed in `android/gradle/libs.versions.toml`. Tests use JUnit 4 in `android/app/src/test/`.

### Core Algorithm (both platforms)

The TextProcessor detects Hanzi characters (Unicode U+4E00–U+9FFF), converts them to Pinyin with tone marks, and preserves non-Hanzi characters (Latin letters, numbers, punctuation, emoji) in place. OCR uses 1-second throttling; text input is debounced at 0.8s.

## Platform Configuration

- **iOS:** Min deployment target uses iOS 15+ Translation API. Info.plist at `ios/BiangBiang-Hanzi-Info.plist`
- **Android:** `minSdk 26`, `targetSdk 36`, `compileSdk 36`, Java 11. Manifest requests CAMERA permission. App ID: `dev.veeso.biangbianghanzi`
