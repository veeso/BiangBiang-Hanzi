@testable import BiangBiang_Hanzi
import Testing

struct ReviewPromptPolicyTests {
    @Test func incrementCapsAtFive() {
        #expect(ReviewPromptPolicy.nextLaunchCount(0) == 1)
        #expect(ReviewPromptPolicy.nextLaunchCount(4) == 5)
        #expect(ReviewPromptPolicy.nextLaunchCount(5) == 5)
        #expect(ReviewPromptPolicy.nextLaunchCount(99) == 5)
    }

    @Test func showsOnlyWhenCountReachedAndNotDismissed() {
        #expect(ReviewPromptPolicy.shouldShow(launchCount: 3, dismissed: false) == true)
        #expect(ReviewPromptPolicy.shouldShow(launchCount: 5, dismissed: false) == true)
        #expect(ReviewPromptPolicy.shouldShow(launchCount: 2, dismissed: false) == false)
        #expect(ReviewPromptPolicy.shouldShow(launchCount: 4, dismissed: true) == false)
    }
}
