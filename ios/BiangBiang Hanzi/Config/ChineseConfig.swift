//
//  ChineseConfig.swift
//  BiangBiang Hanzi
//
//  The complete `BiangBiangConfig` for BiangBiang Hanzi: one Chinese
//  `LanguageProfile` with three variants (Simplified, Traditional,
//  Cantonese). The library renders every screen from this data.
//

import BiangBiangUI

enum ChineseConfig {
    @MainActor
    static let chineseConfig: BiangBiangConfig = .init(
        branding: Branding(
            appName: "BiangBiang Hanzi",
            accentColorHex: "#DE2910",
            logoAssetName: "Logo",
            buttonLogoAssetName: "Logo",
            githubRepo: "veeso/BiangBiang-Hanzi",
            supportEmail: "info@veeso.dev",
            appStoreId: "6754869174",
            playStoreId: "dev.veeso.biangbianghanzi"
        ),
        languages: [
            LanguageProfile(
                id: "chinese",
                displayName: "Chinese variant",
                scriptRanges: [0x4E00 ... 0x9FFF],
                ocrRecognizer: .chinese,
                variants: [
                    LanguageVariant(
                        id: "simplified",
                        displayName: "Simplified",
                        transliterator: PinyinTransliterator(),
                        ttsLanguageCode: "zh-CN",
                        translationLanguageCode: "zh-CN"
                    ),
                    LanguageVariant(
                        id: "traditional",
                        displayName: "Traditional",
                        transliterator: PinyinTransliterator(),
                        ttsLanguageCode: "zh-CN",
                        translationLanguageCode: "zh-CN"
                    ),
                    LanguageVariant(
                        id: "cantonese",
                        displayName: "Cantonese",
                        transliterator: JyutpingTransliterator(),
                        ttsLanguageCode: "zh-HK",
                        translationLanguageCode: "zh-CN"
                    ),
                ]
            ),
        ],
        extraSettings: [],
        plugins: [],
        features: FeatureFlags(),
        strings: [
            "inputTitle": "Hanzi",
            "outputTitle": "Pinyin",
            "appSubtitle": "Convert Hanzi to Pinyin",
        ]
    )
}
