//
//  BiangBiang_HanziApp.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 31/10/25.
//

import SwiftUI

@main
struct BiangBiang_HanziApp: App {
    @State private var settings = AppSettings()
    @State private var audio = AudioPlayerService()

    init() {
        settings.registerLaunch()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(settings)
                .environment(audio)
        }
    }
}
