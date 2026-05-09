package dev.veeso.biangbianghanzi.services

import android.content.Context
import org.json.JSONObject

class JyutpingDictionary private constructor(private val table: Map<String, String>) {

    val size: Int get() = table.size

    fun reading(character: String): String? = table[character]

    companion object {
        @Volatile
        private var instance: JyutpingDictionary? = null

        fun get(context: Context): JyutpingDictionary {
            return instance ?: synchronized(this) {
                instance ?: load(context).also { instance = it }
            }
        }

        private fun load(context: Context): JyutpingDictionary {
            val json = context.assets.open("cantonese.json").bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            return fromJson(json)
        }

        /** Visible for tests. */
        fun fromJson(json: String): JyutpingDictionary {
            val obj = JSONObject(json)
            val map = HashMap<String, String>(obj.length())
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getString(k)
            }
            return JyutpingDictionary(map)
        }
    }
}
