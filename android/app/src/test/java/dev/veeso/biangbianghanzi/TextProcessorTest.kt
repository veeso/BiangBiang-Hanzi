package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.TextProcessor
import org.junit.Assert.assertEquals
import org.junit.Test

class TextProcessorTest {

    @Test
    fun shouldConvertHanziWordToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("你好")
        assertEquals("nĭhăo", pinyin)
    }

    @Test
    fun shouldConvertHanziSentenceToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("我喜欢饺子\uD83E\uDD5F")
        assertEquals("wŏ xĭ huān jiăo zi \uD83E\uDD5F", pinyin)
    }

    @Test
    fun shouldConvertTraditionalHanziSentenceToPinyin() {
        val pinyin = TextProcessor().hanziToPinyin("我喜歡餃子\uD83E\uDD5F")
        assertEquals("wŏ xĭ huān jiăo zi \uD83E\uDD5F", pinyin)

    }

    @Test
    fun shouldProcessTextWithHanzi() {
        val pinyin = TextProcessor().process("我在NASA工作. 现在是5点.")
        assertEquals("wŏzài NASA gōngzuò. xiànzàishì 5 diăn.", pinyin)
    }

}