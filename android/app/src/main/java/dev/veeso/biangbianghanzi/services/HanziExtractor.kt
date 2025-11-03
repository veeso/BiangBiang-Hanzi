package dev.veeso.biangbianghanzi.services

class HanziExtractor {

    fun extract(text: String): String? {
        // Unicode range for CJK Unified Ideographs: U+4E00–U+9FFF
        val pattern = Regex("[\\u4E00-\\u9FFF]+")

        // Find all matches
        val matches = pattern.findAll(text)

        // Join all the Hanzi substrings
        val hanzi = matches.joinToString(separator = "") { it.value }

        // Return null if empty, otherwise the concatenated string
        return hanzi.ifEmpty { null }
    }

}