package dev.veeso.biangbianghanzi.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shared design constants used across the app for a consistent look.
 */
object AppDesign {
    /** Default corner radius for cards and rounded containers. */
    val cornerRadius = 12.dp

    /** Larger corner radius for prominent containers (toasts, banners). */
    val cornerRadiusLarge = 16.dp

    /** Compact corner radius for small surfaces (overlay labels). */
    val cornerRadiusCompact = 6.dp

    /** Spacing between sections within a screen. */
    val sectionSpacing = 20.dp

    /** Standard horizontal padding for content edges. */
    val horizontalPadding = 20.dp

    /** Padding above the bottom safe area for floating controls. */
    val bottomToolbarPadding = 40.dp

    /** Inner spacing for stacks of related controls. */
    val stackSpacing = 12.dp

    /** Standard tap target size. */
    val tapTarget = 44.dp

    /** Shutter button size. */
    val shutterSize = 72.dp

    /** Default duration for short interaction animations. */
    const val SHORT_ANIMATION_MS = 200

    /** Toast fade duration. */
    const val TOAST_ANIMATION_MS = 250

    /** Debounce window for live text input processing. */
    const val INPUT_DEBOUNCE_MS = 800L

    /** Brand red used for active toggles. */
    val accentRed = Color(0xFFDE2910)
}
