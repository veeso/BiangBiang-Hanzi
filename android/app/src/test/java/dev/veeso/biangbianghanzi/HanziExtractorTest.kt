package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.HanziExtractor
import org.junit.Test

import org.junit.Assert.*


class HanziExtractorTest {
    @Test
    fun shouldTakeHanziFromText() {
        val hanzi = HanziExtractor().extract("你好Pizza我爱你")
        assertEquals("你好我爱你", hanzi)
    }

    @Test
    fun shouldNotTakeHanziFromText() {
        val hanzi = HanziExtractor().extract("Pizza123")
        assertNull(hanzi)
    }
}