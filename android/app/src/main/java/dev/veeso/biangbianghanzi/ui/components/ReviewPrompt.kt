package dev.veeso.biangbianghanzi.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.ReviewPromptPolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

private const val TAG = "ReviewPrompt"

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Runs the Play In-App Review flow once. Returns true only if the flow
 * *plausibly* displayed the card, inferred from how long `launchReviewFlow`
 * suspended: Google never reveals whether the card was shown, but a real card
 * requires user interaction (long), while a quota / non-Play no-op completes
 * near-instantly. Logs every stage for diagnosis.
 */
suspend fun launchInAppReview(context: Context): Boolean {
    val activity = context.findActivity()
    if (activity == null) {
        Log.w(TAG, "no Activity from context; aborting")
        return false
    }
    return try {
        val manager = ReviewManagerFactory.create(context)
        Log.d(TAG, "requesting review flow")
        val info = manager.requestReviewFlow().await()
        Log.d(TAG, "requestReviewFlow ok; launching")
        val start = System.currentTimeMillis()
        manager.launchReviewFlow(activity, info).await()
        val elapsed = System.currentTimeMillis() - start
        val shown = ReviewPromptPolicy.looksShown(elapsed)
        Log.d(TAG, "launchReviewFlow completed in ${elapsed}ms looksShown=$shown")
        shown
    } catch (e: Exception) {
        Log.e(TAG, "review flow failed", e)
        false
    }
}

/**
 * Side-effect-only. Once the user has launched the app enough times, request
 * the Play In-App Review flow at a natural pause (app open). The API never
 * confirms the card was shown, so: if it plausibly showed, mark handled; if it
 * no-op'd (quota / non-Play build), keep eligible and retry on later launches,
 * giving up only after MAX_ATTEMPTS so it can't retry forever.
 */
@Composable
fun ReviewPrompt() {
    val context = LocalContext.current
    val repo = remember { AppSettingsRepository(context.applicationContext) }

    val launchCount by repo.reviewLaunchCount.collectAsState(initial = 0)
    val dismissed by repo.reviewPromptDismissed.collectAsState(initial = true)

    LaunchedEffect(launchCount, dismissed) {
        val show = ReviewPromptPolicy.shouldShow(launchCount, dismissed)
        Log.d(TAG, "state launchCount=$launchCount dismissed=$dismissed shouldShow=$show")
        if (!show) return@LaunchedEffect

        val shown = launchInAppReview(context)
        if (shown) {
            repo.dismissReviewForever()
            Log.d(TAG, "card plausibly shown; marked dismissed")
            return@LaunchedEffect
        }

        // Read persisted attempts fresh (not keyed → no same-session retry loop).
        val attemptsBefore = repo.reviewAttempts.first()
        repo.incrementReviewAttempts()
        val attemptsAfter = attemptsBefore + 1
        if (ReviewPromptPolicy.reachedAttemptCap(attemptsAfter)) {
            repo.dismissReviewForever()
            Log.d(TAG, "attempt cap reached ($attemptsAfter); giving up, marked dismissed")
        } else {
            Log.d(TAG, "no-op (attempt $attemptsAfter); staying eligible, will retry")
        }
    }
}
