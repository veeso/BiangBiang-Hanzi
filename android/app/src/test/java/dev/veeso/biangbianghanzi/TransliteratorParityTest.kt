package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.JyutpingDictionary
import dev.veeso.biangbianghanzi.services.JyutpingTransliterator
import dev.veeso.biangbianghanzi.services.PinyinTransliterator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Span-level parity for the two app transliterators. The library's
 * `TextProcessingEngine` owns span detection / passthrough / spacing, so
 * these assert romanisation of an already-isolated script span only.
 *
 * Values match the former Android `TextProcessor.hanziToPinyin` (pinyin4j)
 * — deliberately NOT identical to the iOS expectations: iOS uses
 * `CFStringTransform`, Android uses pinyin4j, each kept at parity with its
 * own former per-platform implementation.
 */
class PinyinTransliteratorParityTest {
    private val t = PinyinTransliterator()

    @Test fun word() = assertEquals("nĭhăo", t.transliterate("你好"))

    @Test fun sentence() = assertEquals("wŏ xĭ huān jiăo zi", t.transliterate("我喜欢饺子"))

    @Test fun traditional() = assertEquals("wŏ xĭ huān jiăo zi", t.transliterate("我喜歡餃子"))

    @Test fun singleCharacter() = assertEquals("wáng", t.transliterate("王"))
}

class JyutpingTransliteratorParityTest {
    private fun translit(): JyutpingTransliterator {
        val json = File("src/main/assets/cantonese.json").readText(Charsets.UTF_8)
        return JyutpingTransliterator(JyutpingDictionary.fromJson(json))
    }

    @Test fun word() = assertEquals("nei5 hou2", translit().transliterate("你好"))

    @Test fun sentence() =
        assertEquals("ngo5 hei2 fun1 sik6 gaau2 zi2", translit().transliterate("我喜歡食餃子"))

    @Test fun unknownCharactersPreservedVerbatim() =
        assertEquals("A hou2 B", translit().transliterate("A好B"))
}
