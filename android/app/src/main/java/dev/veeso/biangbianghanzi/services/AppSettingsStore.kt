package dev.veeso.biangbianghanzi.services

import android.content.Context
import android.os.LocaleList
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.veeso.biangbianghanzi.models.HistoryEntry
import dev.veeso.biangbianghanzi.models.HistoryVariant
import kotlinx.coroutines.flow.map

const val TRADITIONAL_CHINESE = "traditional_chinese"
const val SIMPLIFIED_CHINESE = "simplified_chinese"
const val CANTONESE = "cantonese"

private val Context.dataStore by preferencesDataStore("app_settings")

object AppSettingsKeys {
    val CHINESE_TYPE = stringPreferencesKey("chinese_type")
    val TRANSLATION_LANGUAGE = stringPreferencesKey("translation_language")
    val HISTORY = stringPreferencesKey("history")
}

class AppSettingsRepository(private val context: Context) {

    val chineseType = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.CHINESE_TYPE] ?: SIMPLIFIED_CHINESE
    }

    val translationLanguage = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.TRANSLATION_LANGUAGE] ?: LocaleList.getDefault().get(0).language
    }

    val history = context.dataStore.data.map { prefs ->
        HistorySerializer.fromJson(prefs[AppSettingsKeys.HISTORY] ?: "")
    }

    suspend fun setChineseType(value: String) {
        context.dataStore.edit { it[AppSettingsKeys.CHINESE_TYPE] = value }
    }

    suspend fun setTranslationLanguage(value: String) {
        context.dataStore.edit { it[AppSettingsKeys.TRANSLATION_LANGUAGE] = value }
    }

    suspend fun addHistory(
        original: String,
        transliteration: String,
        variant: HistoryVariant,
    ) {
        context.dataStore.edit { prefs ->
            val current =
                HistorySerializer.fromJson(prefs[AppSettingsKeys.HISTORY] ?: "")
            val entry = HistoryEntry(
                id = java.util.UUID.randomUUID().toString(),
                original = original,
                transliteration = transliteration,
                variant = variant,
                timestamp = System.currentTimeMillis(),
            )
            prefs[AppSettingsKeys.HISTORY] =
                HistorySerializer.toJson(HistoryStore.insert(entry, current))
        }
    }

    suspend fun deleteHistory(id: String) {
        context.dataStore.edit { prefs ->
            val current =
                HistorySerializer.fromJson(prefs[AppSettingsKeys.HISTORY] ?: "")
            prefs[AppSettingsKeys.HISTORY] =
                HistorySerializer.toJson(HistoryStore.delete(id, current))
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[AppSettingsKeys.HISTORY] =
                HistorySerializer.toJson(HistoryStore.clear())
        }
    }
}
