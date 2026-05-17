@testable import BiangBiang_Hanzi
import Foundation
import Testing

@MainActor
struct AppSettingsReviewTests {
    private func makeDefaults() -> UserDefaults {
        let suite = "review-test-\(UUID().uuidString)"
        let d = UserDefaults(suiteName: suite)!
        d.removePersistentDomain(forName: suite)
        return d
    }

    @Test func defaultsAreZeroAndNotDismissed() {
        let s = AppSettings(userDefaults: makeDefaults())
        #expect(s.reviewLaunchCount == 0)
        #expect(s.reviewPromptDismissed == false)
    }

    @Test func registerLaunchIncrementsAndCapsAtFive() {
        let s = AppSettings(userDefaults: makeDefaults())
        for _ in 0 ..< 7 {
            s.registerLaunch()
        }
        #expect(s.reviewLaunchCount == 5)
    }

    @Test func registerLaunchPersists() {
        let d = makeDefaults()
        let a = AppSettings(userDefaults: d)
        a.registerLaunch()
        a.registerLaunch()
        let b = AppSettings(userDefaults: d)
        #expect(b.reviewLaunchCount == 2)
    }

    @Test func notNowResetsCountKeepsDismissedFalse() {
        let s = AppSettings(userDefaults: makeDefaults())
        s.registerLaunch(); s.registerLaunch(); s.registerLaunch()
        s.notNow()
        #expect(s.reviewLaunchCount == 0)
        #expect(s.reviewPromptDismissed == false)
    }

    @Test func dismissForeverPersists() {
        let d = makeDefaults()
        let a = AppSettings(userDefaults: d)
        a.dismissForever()
        #expect(a.reviewPromptDismissed == true)
        let b = AppSettings(userDefaults: d)
        #expect(b.reviewPromptDismissed == true)
    }
}
