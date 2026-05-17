package dev.veeso.biangbianghanzi.services

import android.content.Context
import android.os.LocaleList
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
    val REVIEW_LAUNCH_COUNT = intPreferencesKey("review_launch_count")
    val REVIEW_PROMPT_DISMISSED = booleanPreferencesKey("review_prompt_dismissed")
    val REVIEW_ATTEMPTS = intPreferencesKey("review_attempts")
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

    val reviewLaunchCount = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.REVIEW_LAUNCH_COUNT] ?: 0
    }

    val reviewPromptDismissed = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.REVIEW_PROMPT_DISMISSED] ?: false
    }

    val reviewAttempts = context.dataStore.data.map { prefs ->
        prefs[AppSettingsKeys.REVIEW_ATTEMPTS] ?: 0
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

    /** Increment the cold-launch counter, capped per policy. Call once per cold start. */
    suspend fun registerLaunch() {
        context.dataStore.edit { prefs ->
            val current = prefs[AppSettingsKeys.REVIEW_LAUNCH_COUNT] ?: 0
            prefs[AppSettingsKeys.REVIEW_LAUNCH_COUNT] =
                ReviewPromptPolicy.nextLaunchCount(current)
        }
    }

    /** Mark the review prompt handled so it never fires again. */
    suspend fun dismissReviewForever() {
        context.dataStore.edit { it[AppSettingsKeys.REVIEW_PROMPT_DISMISSED] = true }
    }

    /** Record a native-flow attempt that did not visibly show; retried later. */
    suspend fun incrementReviewAttempts() {
        context.dataStore.edit { prefs ->
            prefs[AppSettingsKeys.REVIEW_ATTEMPTS] =
                (prefs[AppSettingsKeys.REVIEW_ATTEMPTS] ?: 0) + 1
        }
    }

    /** Debug aid: clear dismissed flag and launch count so the prompt can re-trigger. */
    suspend fun resetReviewPrompt() {
        context.dataStore.edit { prefs ->
            prefs[AppSettingsKeys.REVIEW_LAUNCH_COUNT] = 0
            prefs[AppSettingsKeys.REVIEW_PROMPT_DISMISSED] = false
            prefs[AppSettingsKeys.REVIEW_ATTEMPTS] = 0
        }
    }
}
