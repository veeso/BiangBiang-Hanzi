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
    static let simplifiedChinese = "zh-Hans"
    static let traditionalChinese = "zh-Hant"
    static let cantonese = "yue"

    var userLanguage: String {
        didSet {
            UserDefaults.standard.set(userLanguage, forKey: "user_language")
        }
    }

    var chineseVariant: String {
        didSet { UserDefaults.standard.set(chineseVariant, forKey: "chinese") }
    }

    var isCantonese: Bool {
        chineseVariant == Self.cantonese
    }

    var history: [HistoryEntry] {
        didSet {
            if let data = try? JSONEncoder().encode(history) {
                userDefaults.set(data, forKey: "history")
            }
        }
    }

    var reviewLaunchCount: Int {
        didSet { userDefaults.set(reviewLaunchCount, forKey: "review_launch_count") }
    }

    var reviewPromptDismissed: Bool {
        didSet { userDefaults.set(reviewPromptDismissed, forKey: "review_prompt_dismissed") }
    }

    private let userDefaults: UserDefaults

    init(
        userDefaults: UserDefaults = .standard,
        defaultLanguage: String = Locale.current.language.languageCode?
            .identifier ?? "en",
        defaultChineseVariant: String = "zh-Hans"
    ) {
        self.userDefaults = userDefaults
        userLanguage =
            userDefaults.string(forKey: "user_language") ?? defaultLanguage
        chineseVariant =
            userDefaults.string(forKey: "chinese") ?? defaultChineseVariant
        if let data = userDefaults.data(forKey: "history"),
           let decoded = try? JSONDecoder().decode([HistoryEntry].self, from: data)
        {
            history = decoded
        } else {
            history = []
        }
        reviewLaunchCount = userDefaults.integer(forKey: "review_launch_count")
        reviewPromptDismissed = userDefaults.bool(forKey: "review_prompt_dismissed")
    }

    func addHistory(original: String, transliteration: String, variant: HistoryVariant) {
        let entry = HistoryEntry(original: original, transliteration: transliteration, variant: variant)
        history = HistoryStore.insert(entry, into: history)
    }

    func deleteHistory(id: UUID) {
        history = HistoryStore.delete(id: id, from: history)
    }

    func clearHistory() {
        history = HistoryStore.clear()
    }

    /// Increment the cold-launch counter, capped per policy. Call once per process launch.
    func registerLaunch() {
        reviewLaunchCount = ReviewPromptPolicy.nextLaunchCount(reviewLaunchCount)
    }

    /// "Not now": reset the counter, keep prompting after 3 more launches.
    func notNow() {
        reviewLaunchCount = 0
    }

    /// "Rate now" / "Don't ask again": never show the prompt again.
    func dismissForever() {
        reviewPromptDismissed = true
    }
}
