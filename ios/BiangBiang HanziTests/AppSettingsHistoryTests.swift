@testable import BiangBiang_Hanzi
import Foundation
import Testing

@MainActor
struct AppSettingsHistoryTests {
    private func makeDefaults() -> UserDefaults {
        let suite = "history-test-\(UUID().uuidString)"
        let d = UserDefaults(suiteName: suite)!
        d.removePersistentDomain(forName: suite)
        return d
    }

    @Test func defaultHistoryIsEmpty() {
        let settings = AppSettings(userDefaults: makeDefaults())
        #expect(settings.history.isEmpty)
    }

    @Test func addHistoryDedupsConsecutive() {
        let settings = AppSettings(userDefaults: makeDefaults())
        settings.addHistory(original: "你好", transliteration: "nǐ hǎo", variant: .mandarin)
        settings.addHistory(original: "你好", transliteration: "nǐ hǎo", variant: .mandarin)
        #expect(settings.history.count == 1)
    }

    @Test func historyRoundTripsThroughUserDefaults() {
        let defaults = makeDefaults()
        let a = AppSettings(userDefaults: defaults)
        a.addHistory(original: "我", transliteration: "wǒ", variant: .cantonese)
        let b = AppSettings(userDefaults: defaults)
        #expect(b.history.count == 1)
        #expect(b.history.first?.original == "我")
        #expect(b.history.first?.variant == .cantonese)
    }

    @Test func clearHistoryEmptiesAndPersists() {
        let defaults = makeDefaults()
        let a = AppSettings(userDefaults: defaults)
        a.addHistory(original: "我", transliteration: "wǒ", variant: .mandarin)
        a.clearHistory()
        let b = AppSettings(userDefaults: defaults)
        #expect(b.history.isEmpty)
    }
}
