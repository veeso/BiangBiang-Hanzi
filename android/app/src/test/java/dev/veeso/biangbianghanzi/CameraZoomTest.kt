package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.availablePresets
import dev.veeso.biangbianghanzi.services.clampZoom
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraZoomTest {

    @Test
    fun presetsEmptyWhenMaxBelowOne() {
        assertEquals(emptyList<Float>(), availablePresets(maxZoom = 0.8f))
    }

    @Test
    fun presetsOnlyOneWhenMaxIsOne() {
        assertEquals(listOf(1f), availablePresets(maxZoom = 1.0f))
    }

    @Test
    fun presetsOneAndTwoWhenMaxBetweenTwoAndFive() {
        assertEquals(listOf(1f, 2f), availablePresets(maxZoom = 3.4f))
    }

    @Test
    fun presetsAllThreeWhenMaxAtLeastFive() {
        assertEquals(listOf(1f, 2f, 5f), availablePresets(maxZoom = 5.0f))
        assertEquals(listOf(1f, 2f, 5f), availablePresets(maxZoom = 10.0f))
    }

    @Test
    fun clampZoomLowerBound() {
        assertEquals(1.0f, clampZoom(0.2f, 1.0f, 5.0f), 0.0001f)
    }

    @Test
    fun clampZoomUpperBound() {
        assertEquals(5.0f, clampZoom(8.0f, 1.0f, 5.0f), 0.0001f)
    }

    @Test
    fun clampZoomWithinRange() {
        assertEquals(2.5f, clampZoom(2.5f, 1.0f, 5.0f), 0.0001f)
    }
}
