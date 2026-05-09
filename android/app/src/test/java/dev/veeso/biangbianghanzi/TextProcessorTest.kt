package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.JyutpingDictionary
import dev.veeso.biangbianghanzi.services.TextProcessor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TextProcessorTest {

    private fun cantoneseProcessor(): TextProcessor {
        val json = File("src/main/assets/cantonese.json").readText(Charsets.UTF_8)
        return TextProcessor(jyutping = JyutpingDictionary.fromJson(json))
    }

    @Test
    fun shouldConvertHanziWordToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("你好")
        assertEquals("nĭhăo", pinyin)
    }

    @Test
    fun shouldConvertHanziSentenceToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("我喜欢饺子🥟")
        assertEquals("wŏ xĭ huān jiăo zi 🥟", pinyin)
    }

    @Test
    fun shouldConvertTraditionalHanziSentenceToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("我喜歡餃子🥟")
        assertEquals("wŏ xĭ huān jiăo zi 🥟", pinyin)
    }

    @Test
    fun shouldProcessTextWithHanzi() {
        val pinyin = TextProcessor().process("我在NASA工作. 现在是5点.")
        assertEquals("wŏzài NASA gōngzuò. xiànzàishì 5 diăn.", pinyin)
    }

    @Test
    fun shouldConvertHanziToJyutping() {
        val processor = cantoneseProcessor()
        assertEquals("nei5 hou2", processor.hanziToJyutping("你好"))
    }

    @Test
    fun shouldProcessCantoneseSentenceWithUnknownChars() {
        val processor = cantoneseProcessor()
        val result = processor.process("A好B", TextProcessor.Mode.CANTONESE)
        assertEquals("A hou2 B", result)
    }
}
