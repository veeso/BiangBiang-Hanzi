//
//  BiangBiang_HanziApp.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 31/10/25.
//
//  Config-only entry point: the BiangBiangUI library renders every screen
//  and owns History, the rate prompt, TTS and the OCR pipeline. The app
//  supplies only `ChineseConfig` + two transliterators.
//

import BiangBiangUI
import SwiftUI

@main
struct BiangBiang_HanziApp: App {
    var body: some Scene {
        WindowGroup {
            BiangBiangRootView(config: ChineseConfig.chineseConfig)
        }
    }
}
