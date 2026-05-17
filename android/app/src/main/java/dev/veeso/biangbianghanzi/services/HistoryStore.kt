package dev.veeso.biangbianghanzi.services

import dev.veeso.biangbianghanzi.models.HistoryEntry

/** Pure history-list mutation rules. No persistence, no UI. */
object HistoryStore {
    /** Silent safety cap. Never surfaced in UI. */
    const val SAFETY_CAP = 500

    fun insert(
        entry: HistoryEntry,
        list: List<HistoryEntry>,
    ): List<HistoryEntry> {
        val newest = list.firstOrNull()
        if (newest != null &&
            newest.original == entry.original &&
            newest.variant == entry.variant
        ) {
            return list
        }
        val result = ArrayList<HistoryEntry>(list.size + 1)
        result.add(entry)
        result.addAll(list)
        return if (result.size > SAFETY_CAP) {
            result.subList(0, SAFETY_CAP).toList()
        } else {
            result
        }
    }

    fun delete(
        id: String,
        list: List<HistoryEntry>,
    ): List<HistoryEntry> = list.filterNot { it.id == id }

    fun clear(): List<HistoryEntry> = emptyList()
}
