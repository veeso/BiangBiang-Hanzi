package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.models.HistoryEntry
import dev.veeso.biangbianghanzi.models.HistoryVariant
import dev.veeso.biangbianghanzi.services.HistorySerializer
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySerializerTest {
    @Test
    fun roundTripsList() {
        val list = listOf(
            HistoryEntry("id1", "你好", "nei5 hou2", HistoryVariant.CANTONESE, 1234L),
            HistoryEntry("id2", "我", "wǒ", HistoryVariant.MANDARIN, 5678L),
        )
        val json = HistorySerializer.toJson(list)
        val back = HistorySerializer.fromJson(json)
        assertEquals(list, back)
    }

    @Test
    fun fromBlankReturnsEmpty() {
        assertEquals(emptyList<HistoryEntry>(), HistorySerializer.fromJson(""))
    }

    @Test
    fun fromMalformedReturnsEmpty() {
        assertEquals(emptyList<HistoryEntry>(), HistorySerializer.fromJson("not-json"))
    }
}
