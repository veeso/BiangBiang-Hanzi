package dev.veeso.biangbianghanzi.services

import dev.veeso.biangbiangui.protocols.Transliterator

/**
 * Cantonese romanisation. Per-character dictionary lookup, unknown
 * characters preserved verbatim, tokens joined by single spaces
 * (faithfully the former `TextProcessor.hanziToJyutping`). Span detection
 * and spacing are owned by the library's engine.
 */
class JyutpingTransliterator(private val dictionary: JyutpingDictionary) : Transliterator {

    override fun transliterate(scriptSpan: String): String {
        val out = StringBuilder()
        for (ch in scriptSpan) {
            val key = ch.toString()
            val reading = dictionary.reading(key) ?: key
            if (out.isNotEmpty()) out.append(' ')
            out.append(reading)
        }
        return out.toString()
    }
}
