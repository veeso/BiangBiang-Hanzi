package dev.veeso.biangbianghanzi.services

/**
 * Filter the candidate zoom presets to those supported by the device.
 */
fun availablePresets(
    maxZoom: Float,
    candidates: List<Float> = listOf(1f, 2f, 5f),
): List<Float> = candidates.filter { it <= maxZoom }

/**
 * Clamp a zoom factor to a closed range.
 */
fun clampZoom(value: Float, min: Float, max: Float): Float =
    value.coerceIn(min, max)
