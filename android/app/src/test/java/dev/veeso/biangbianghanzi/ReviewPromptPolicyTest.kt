package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.ReviewPromptPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPromptPolicyTest {
    @Test
    fun nextLaunchCountCapsAtFive() {
        assertEquals(1, ReviewPromptPolicy.nextLaunchCount(0))
        assertEquals(5, ReviewPromptPolicy.nextLaunchCount(4))
        assertEquals(5, ReviewPromptPolicy.nextLaunchCount(5))
        assertEquals(5, ReviewPromptPolicy.nextLaunchCount(99))
    }

    @Test
    fun shouldShowOnlyWhenThresholdReachedAndNotDismissed() {
        assertTrue(ReviewPromptPolicy.shouldShow(launchCount = 5, dismissed = false))
        assertFalse(ReviewPromptPolicy.shouldShow(launchCount = 4, dismissed = false))
        assertFalse(ReviewPromptPolicy.shouldShow(launchCount = 3, dismissed = false))
        assertFalse(ReviewPromptPolicy.shouldShow(launchCount = 5, dismissed = true))
    }

    @Test
    fun reachedAttemptCapAtMax() {
        assertFalse(ReviewPromptPolicy.reachedAttemptCap(0))
        assertFalse(ReviewPromptPolicy.reachedAttemptCap(ReviewPromptPolicy.MAX_ATTEMPTS - 1))
        assertTrue(ReviewPromptPolicy.reachedAttemptCap(ReviewPromptPolicy.MAX_ATTEMPTS))
        assertTrue(ReviewPromptPolicy.reachedAttemptCap(ReviewPromptPolicy.MAX_ATTEMPTS + 3))
    }

    @Test
    fun looksShownOnlyWhenFlowSuspendedLongEnough() {
        assertFalse(ReviewPromptPolicy.looksShown(0L))
        assertFalse(ReviewPromptPolicy.looksShown(ReviewPromptPolicy.SHOWN_MIN_ELAPSED_MS - 1))
        assertTrue(ReviewPromptPolicy.looksShown(ReviewPromptPolicy.SHOWN_MIN_ELAPSED_MS))
        assertTrue(ReviewPromptPolicy.looksShown(5_000L))
    }
}
