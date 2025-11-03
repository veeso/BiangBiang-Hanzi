package dev.veeso.biangbianghanzi

import dev.veeso.biangbianghanzi.services.PinyinConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class PinyinConverterTest {
    @Test
    fun shouldConvertHanziWordToPinyin() {
        val pinyin = PinyinConverter().hanziToPinyin("你好")
        assertEquals("nĭhăo", pinyin)
    }

    @Test
    fun shouldConvertHanziSentenceToPinyin() {
        val pinyin = PinyinConverter().hanziToPinyin("我喜欢饺子\uD83E\uDD5F")
        assertEquals("wŏ xĭ huān jiăo zi \uD83E\uDD5F", pinyin)
    }

    @Test
    fun shouldConvertTraditionalHanziSentenceToPinyin() {
        val pinyin = PinyinConverter().hanziToPinyin("我喜歡餃子\uD83E\uDD5F")
        assertEquals("wŏ xĭ huān jiăo zi \uD83E\uDD5F", pinyin)

    }
}