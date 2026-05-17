package dev.veeso.biangbianghanzi.services

import dev.veeso.biangbianghanzi.models.HistoryEntry
import dev.veeso.biangbianghanzi.models.HistoryVariant
import org.json.JSONArray
import org.json.JSONObject

/** org.json (de)serialization for the persisted history list. */
object HistorySerializer {
    fun toJson(list: List<HistoryEntry>): String {
        val arr = JSONArray()
        for (e in list) {
            val o = JSONObject()
            o.put("id", e.id)
            o.put("original", e.original)
            o.put("transliteration", e.transliteration)
            o.put("variant", e.variant.name)
            o.put("timestamp", e.timestamp)
            arr.put(o)
        }
        return arr.toString()
    }

    fun fromJson(json: String): List<HistoryEntry> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        HistoryEntry(
                            id = o.getString("id"),
                            original = o.getString("original"),
                            transliteration = o.getString("transliteration"),
                            variant = HistoryVariant.valueOf(o.getString("variant")),
                            timestamp = o.getLong("timestamp"),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
