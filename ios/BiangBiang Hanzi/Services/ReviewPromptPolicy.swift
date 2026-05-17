//
//  ReviewPromptPolicy.swift
//  BiangBiang Hanzi
//

import Foundation

/// Pure, side-effect-free decision logic for the "rate the app" prompt.
enum ReviewPromptPolicy {
    /// Minimum cold launches before the prompt is eligible to show.
    static let launchThreshold = 3
    /// Counter cap to avoid unbounded writes.
    static let launchCap = 5

    static func nextLaunchCount(_ current: Int) -> Int {
        min(current + 1, launchCap)
    }

    static func shouldShow(launchCount: Int, dismissed: Bool) -> Bool {
        !dismissed && launchCount >= launchThreshold
    }
}
