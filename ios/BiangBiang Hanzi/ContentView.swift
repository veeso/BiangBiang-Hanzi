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

    @State private var selection: AppTab = .text

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
    }
}

#Preview {
    ContentView().environment(AppSettings())
}
