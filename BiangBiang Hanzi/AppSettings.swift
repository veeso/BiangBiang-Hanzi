//
//  AppSettings.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Combine
import Foundation

@MainActor
final class AppSettings: ObservableObject {
    @Published var userLanguage: String {
        didSet { UserDefaults.standard.set(userLanguage, forKey: "user_language") }
    }

    @Published var chineseVariant: String {
        didSet { UserDefaults.standard.set(chineseVariant, forKey: "chinese") }
    }

    init(
        userDefaults: UserDefaults = .standard,
        defaultLanguage: String = Locale.current.language.languageCode?.identifier ?? "en",
        defaultChineseVariant: String = "simplified"
    ) {
        userLanguage = userDefaults.string(forKey: "user_language") ?? defaultLanguage
        chineseVariant = userDefaults.string(forKey: "chinese") ?? defaultChineseVariant
    }
}
