package dev.veeso.biangbianghanzi.ui.screens.textmode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dev.veeso.biangbianghanzi.services.TextProcessor
import dev.veeso.biangbianghanzi.ui.AppDesign
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TextModeViewModel : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _pinyinText = MutableStateFlow("")
    val pinyinText = _pinyinText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText = _translatedText.asStateFlow()

    private val textProcessor = TextProcessor()
    private var debounceJob: Job? = null

    fun onInputChanged(newText: String) {
        _inputText.value = newText
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(AppDesign.INPUT_DEBOUNCE_MS)
            processInput()
        }
    }

    private fun processInput() {
        val text = _inputText.value
        if (text.trim().isEmpty()) {
            _pinyinText.value = ""
            _translatedText.value = ""
            return
        }
        _pinyinText.value = textProcessor.hanziToPinyin(text)
    }

    fun translate(userLanguage: String) {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.CHINESE)
            .setTargetLanguage(userLanguage)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        _translatedText.value = translated
                    }
                    .addOnFailureListener { e ->
                        _translatedText.value = "⚠️ Translation failed: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _translatedText.value = "⚠️ Model download failed: ${e.message}"
            }
    }

    fun copyToClipboard(context: Context, text: String) {
        if (text.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
    }
}
