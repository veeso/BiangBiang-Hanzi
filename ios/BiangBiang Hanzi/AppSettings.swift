//
//  AppSettings.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Foundation
import Observation

@MainActor
@Observable
final class AppSettings {
    var userLanguage: String {
        didSet {
            UserDefaults.standard.set(userLanguage, forKey: "user_language")
        }
    }

    var chineseVariant: String {
        didSet { UserDefaults.standard.set(chineseVariant, forKey: "chinese") }
    }

    init(
        userDefaults: UserDefaults = .standard,
        defaultLanguage: String = Locale.current.language.languageCode?
            .identifier ?? "en",
        defaultChineseVariant: String = "zh-Hans"
    ) {
        userLanguage =
            userDefaults.string(forKey: "user_language") ?? defaultLanguage
        chineseVariant =
            userDefaults.string(forKey: "chinese") ?? defaultChineseVariant
    }
}
