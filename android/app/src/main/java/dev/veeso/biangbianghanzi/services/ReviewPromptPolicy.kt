package dev.veeso.biangbianghanzi.services

/** Pure, side-effect-free decision logic for the "rate the app" prompt. */
object ReviewPromptPolicy {
    /**
     * Minimum cold launches before the prompt is eligible. Set to the cap (5)
     * because the Play In-App Review flow is one-shot with no opt-out or
     * re-prompt: spend the single chance on an engaged user.
     */
    const val LAUNCH_THRESHOLD = 5

    /** Counter cap to avoid unbounded writes. */
    const val LAUNCH_CAP = 5

    /**
     * Max times the native flow may be attempted before giving up. The Play
     * In-App Review API never reports whether the card was actually shown
     * (quota / non-Play build → silent no-op), so we retry across launches and
     * stop after this many attempts to avoid retrying forever.
     */
    const val MAX_ATTEMPTS = 5

    /**
     * Below this elapsed time (ms) for launchReviewFlow, assume the card was
     * NOT shown: a real card requires user interaction and suspends far longer;
     * a quota/non-Play no-op completes near-instantly.
     */
    const val SHOWN_MIN_ELAPSED_MS = 800L

    fun nextLaunchCount(current: Int): Int = minOf(current + 1, LAUNCH_CAP)

    fun shouldShow(launchCount: Int, dismissed: Boolean): Boolean =
        !dismissed && launchCount >= LAUNCH_THRESHOLD

    /** True once attempts exhausted; give up so the flow stops retrying. */
    fun reachedAttemptCap(attempts: Int): Boolean = attempts >= MAX_ATTEMPTS

    /** Heuristic: a plausibly-shown card suspends the flow at least this long. */
    fun looksShown(elapsedMs: Long): Boolean = elapsedMs >= SHOWN_MIN_ELAPSED_MS
}
