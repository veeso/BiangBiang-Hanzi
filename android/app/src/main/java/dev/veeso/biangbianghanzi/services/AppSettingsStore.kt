package dev.veeso.biangbianghanzi.services

import android.content.Context
import android.os.LocaleList
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

const val TRADITIONAL_CHINESE = "traditional_chinese";
const val SIMPLIFIED_CHINESE = "simplified_chinese";

private val Context.dataStore by preferencesDataStore("app_settings")

object AppSettingsKeys {
    val CHINESE_TYPE = stringPreferencesKey("chinese_type")
    val TRANSLATION_LANGUAGE = stringPreferencesKey("translation_language")
}

class AppSettingsRepository(private val context: Context) {

    val chineseType = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.CHINESE_TYPE] ?: SIMPLIFIED_CHINESE
    }

    val translationLanguage = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.TRANSLATION_LANGUAGE] ?: LocaleList.getDefault().get(0).language
    }

    suspend fun setChineseType(value: String) {
        context.dataStore.edit { it[AppSettingsKeys.CHINESE_TYPE] = value }
    }

    suspend fun setTranslationLanguage(value: String) {
        context.dataStore.edit { it[AppSettingsKeys.TRANSLATION_LANGUAGE] = value }
    }
}
