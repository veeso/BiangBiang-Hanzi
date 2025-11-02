//
//  BiangBiang_HanziApp.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 31/10/25.
//

import SwiftUI

@main
struct BiangBiang_HanziApp: App {
    @StateObject private var settings = AppSettings()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(settings)
        }
    }
}
