package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.JyutpingDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class JyutpingDictionaryTest {

    private fun loadFromAssets(): JyutpingDictionary {
        val json = File("src/main/assets/cantonese.json").readText(Charsets.UTF_8)
        return JyutpingDictionary.fromJson(json)
    }

    @Test
    fun shouldLoadDictionary() {
        val dict = loadFromAssets()
        assertTrue("size > 5000", dict.size > 5000)
    }

    @Test
    fun shouldLookupKnownCharacter() {
        val dict = loadFromAssets()
        assertEquals("zung1", dict.reading("中"))
        assertEquals("zi6", dict.reading("字"))
    }

    @Test
    fun shouldReturnNullForUnknownCharacter() {
        val dict = loadFromAssets()
        assertNull(dict.reading("A"))
    }
}
