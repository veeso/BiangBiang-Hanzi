package dev.veeso.biangbianghanzi.ui.screens.textmode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dev.veeso.biangbianghanzi.services.PinyinConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TextModeViewModel : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _pinyinText = MutableStateFlow("")
    val pinyinText = _pinyinText.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText = _translatedText.asStateFlow()

    fun onInputChanged(newText: String) {
        _inputText.value = newText
        _pinyinText.value = PinyinConverter().hanziToPinyin(newText)
    }

    fun translate(userLanguage: String) {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.CHINESE)
            .setTargetLanguage(userLanguage)
            .build()

        val translator = Translation.getClient(options)

        // Download model if required
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
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }
}
