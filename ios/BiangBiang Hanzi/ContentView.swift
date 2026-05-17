//
//  ContentView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 31/10/25.
//

import SwiftUI

struct ContentView: View {
    enum AppTab: Hashable {
        case text
        case camera
        case settings
        case history
    }

    static let appStoreID = "6754869174"
    static let writeReviewURL = URL(
        string: "https://apps.apple.com/app/id6754869174?action=write-review"
    )!

    @Environment(AppSettings.self) private var settings
    @Environment(\.openURL) private var openURL
    @State private var selection: AppTab = .text
    @State private var showReviewPrompt = false

    var body: some View {
        TabView(selection: $selection) {
            Tab("Text", systemImage: "textformat", value: AppTab.text) {
                TextModeView()
            }
            Tab("Camera", systemImage: "camera", value: AppTab.camera) {
                CameraModeView()
            }
            Tab("History", systemImage: "clock.fill", value: AppTab.history) {
                HistoryView()
            }
            Tab("Settings", systemImage: "gear", value: AppTab.settings) {
                SettingsView()
            }
        }
        .task {
            showReviewPrompt = ReviewPromptPolicy.shouldShow(
                launchCount: settings.reviewLaunchCount,
                dismissed: settings.reviewPromptDismissed
            )
        }
        .alert("Enjoying BiangBiang Hanzi?", isPresented: $showReviewPrompt) {
            Button("Rate now") {
                settings.dismissForever()
                openURL(Self.writeReviewURL)
            }
            Button("Not now", role: .cancel) {
                settings.notNow()
            }
            Button("Don't ask again", role: .destructive) {
                settings.dismissForever()
            }
        } message: {
            Text(
                "If BiangBiang Hanzi has been helpful, a quick rating on the App Store really helps. It only takes a moment."
            )
        }
    }
}

#Preview {
    ContentView().environment(AppSettings())
}
