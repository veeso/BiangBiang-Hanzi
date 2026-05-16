package dev.veeso.biangbianghanzi.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Speaks Hanzi text aloud using the platform [TextToSpeech] engine.
 *
 * Mirrors the iOS `AudioPlayerService`: a UI-agnostic, app-scoped speech
 * service exposing a [State] and `speak`/`stop` controls, with the synthesis
 * voice selected from the Chinese variant setting.
 */
class AudioPlayerService(context: Context) {

    enum class State {
        IDLE,
        SPEAKING,
    }

    enum class Language(val locale: Locale) {
        /** Mandarin — BCP-47 `zh-CN`. */
        MANDARIN(Locale.forLanguageTag("zh-CN")),

        /** Cantonese — BCP-47 `zh-HK`. */
        CANTONESE(Locale.forLanguageTag("zh-HK")),
    }

    private val _state = MutableStateFlow(State.IDLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private var initialized = false
    private var pending: Pair<String, Language>? = null

    private val tts = TextToSpeech(context.applicationContext) { status ->
        initialized = status == TextToSpeech.SUCCESS
        if (initialized) {
            pending?.let { (text, language) -> enqueue(text, language) }
        }
        pending = null
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = State.SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                _state.value = State.IDLE
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _state.value = State.IDLE
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                _state.value = State.IDLE
            }
        })
    }

    /**
     * Speaks [text] aloud using the system speech synthesizer.
     *
     * Any in-progress playback is stopped first. Empty or whitespace-only
     * input is ignored. If the engine has not finished initializing the
     * request is queued and spoken once ready.
     */
    fun speak(text: String, language: Language) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        stop()
        if (!initialized) {
            pending = trimmed to language
            return
        }
        enqueue(trimmed, language)
    }

    /** Stops any active speech and resets [state] to [State.IDLE]. */
    fun stop() {
        pending = null
        if (initialized) tts.stop()
        _state.value = State.IDLE
    }

    /** Releases the underlying engine. Call from the owner's lifecycle end. */
    fun shutdown() {
        tts.shutdown()
        initialized = false
        _state.value = State.IDLE
    }

    private fun enqueue(text: String, language: Language) {
        tts.language = language.locale
        _state.value = State.SPEAKING
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    private companion object {
        const val UTTERANCE_ID = "biangbiang-hanzi-tts"
    }
}
