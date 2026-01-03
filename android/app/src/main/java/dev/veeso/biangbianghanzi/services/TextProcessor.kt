package dev.veeso.biangbianghanzi.services

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

class TextProcessor {

    // Unicode range for CJK Unified Ideographs: U+4E00–U+9FFF
    val pattern = Regex("[\\u4E00-\\u9FFF]+")

    val format: HanyuPinyinOutputFormat = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_MARK
        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    fun process(text: String): String? {
        if (!pattern.containsMatchIn(text)) {
            return null
        }

        val matches = pattern.findAll(text).toList().asReversed()
        val result = StringBuilder(text)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            val hanzi = result.substring(start, end)
            val pinyin = hanziToPinyin(hanzi)

            // Space before
            val needsLeadingSpace = when {
                start == 0 -> false
                result[start - 1] == ' ' -> false
                ".,!?;:。，".contains(result[start - 1]) -> false
                else -> true
            }

            // Space after
            val needsTrailingSpace = when {
                end >= result.length -> false
                else -> {
                    val nextChar = result[end]
                    nextChar.isLetterOrDigit() && nextChar.code < 128
                }
            }

            val replacement = buildString {
                if (needsLeadingSpace) append(' ')
                append(pinyin)
                if (needsTrailingSpace) append(' ')
            }

            result.replace(start, end, replacement)
        }

        // Cleanup pass
        return result
            .toString()
            .trim()
    }

    fun hanziToPinyin(text: String): String {
        return PinyinHelper.toHanYuPinyinString(
            text, format,
            " ",
            true
        )
    }

}