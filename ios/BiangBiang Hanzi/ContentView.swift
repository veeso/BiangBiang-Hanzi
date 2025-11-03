//
//  ContentView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 31/10/25.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            TextModeView()
                .tabItem {
                    Label("Text", systemImage: "textformat")
                }

            CameraModeView()
                .tabItem {
                    Label("Camera", systemImage: "camera")
                }

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}

#Preview {
    ContentView().environmentObject(AppSettings())
}
