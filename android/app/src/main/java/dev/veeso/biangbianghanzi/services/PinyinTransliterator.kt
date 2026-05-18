package dev.veeso.biangbianghanzi.services

import dev.veeso.biangbiangui.protocols.Transliterator
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

/**
 * Mandarin romanisation. The library's
 * [dev.veeso.biangbiangui.services.TextProcessingEngine] owns span
 * detection, passthrough and spacing; this only romanises one isolated
 * script span via `pinyin4j` (faithfully the former
 * `TextProcessor.hanziToPinyin`). `pinyin4j` is an app-only dependency,
 * never referenced by the library.
 */
class PinyinTransliterator : Transliterator {

    private val format: HanyuPinyinOutputFormat = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_MARK
        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    override fun transliterate(scriptSpan: String): String =
        PinyinHelper.toHanYuPinyinString(scriptSpan, format, " ", true)
            .trim()
}
