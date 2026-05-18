package dev.veeso.biangbianghanzi.config

import android.content.Context
import dev.veeso.biangbianghanzi.services.JyutpingDictionary
import dev.veeso.biangbianghanzi.services.JyutpingTransliterator
import dev.veeso.biangbianghanzi.services.PinyinTransliterator
import dev.veeso.biangbiangui.config.BiangBiangConfig
import dev.veeso.biangbiangui.config.Branding
import dev.veeso.biangbiangui.config.FeatureFlags
import dev.veeso.biangbiangui.config.LanguageProfile
import dev.veeso.biangbiangui.config.LanguageVariant
import dev.veeso.biangbiangui.config.OcrRecognizer

/**
 * The complete [BiangBiangConfig] for BiangBiang Hanzi: one Chinese
 * [LanguageProfile] with three variants (Simplified, Traditional,
 * Cantonese). The library renders every screen from this data.
 *
 * A factory function (not a constant) because [JyutpingDictionary] needs a
 * [Context] to load `cantonese.json` from assets. Logo asset names are the
 * app's `drawable` resource names (`logo`, `logo_button_ico`).
 */
fun chineseConfig(context: Context): BiangBiangConfig {
    val pinyin = PinyinTransliterator()
    val jyutping = JyutpingTransliterator(JyutpingDictionary.get(context.applicationContext))
    return BiangBiangConfig(
        branding = Branding(
            appName = "BiangBiang Hanzi",
            accentColorHex = "#DE2910",
            logoAssetName = "logo",
            buttonLogoAssetName = "logo_button_ico",
            githubRepo = "veeso/BiangBiang-Hanzi",
            supportEmail = "info@veeso.dev",
            appStoreId = "6754869174",
            playStoreId = "dev.veeso.biangbianghanzi",
        ),
        languages = listOf(
            LanguageProfile(
                id = "chinese",
                displayName = "Chinese",
                scriptRanges = listOf(0x4E00u..0x9FFFu),
                ocrRecognizer = OcrRecognizer.CHINESE,
                variants = listOf(
                    LanguageVariant(
                        id = "simplified",
                        displayName = "Simplified",
                        transliterator = pinyin,
                        ttsLanguageCode = "zh-CN",
                        translatable = true,
                    ),
                    LanguageVariant(
                        id = "traditional",
                        displayName = "Traditional",
                        transliterator = pinyin,
                        ttsLanguageCode = "zh-CN",
                        translatable = true,
                    ),
                    LanguageVariant(
                        id = "cantonese",
                        displayName = "Cantonese",
                        transliterator = jyutping,
                        ttsLanguageCode = "zh-HK",
                        translatable = false,
                    ),
                ),
            ),
        ),
        extraSettings = emptyList(),
        plugins = emptyList(),
        features = FeatureFlags(),
        strings = mapOf(
            "inputTitle" to "Hanzi",
            "outputTitle" to "Pinyin",
            "appSubtitle" to "Convert Hanzi to Pinyin",
        ),
    )
}
