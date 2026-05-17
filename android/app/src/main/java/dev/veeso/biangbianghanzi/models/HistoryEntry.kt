package dev.veeso.biangbianghanzi.models

enum class HistoryVariant { MANDARIN, CANTONESE }

data class HistoryEntry(
    val id: String,
    val original: String,
    val transliteration: String,
    val variant: HistoryVariant,
    val timestamp: Long,
)
