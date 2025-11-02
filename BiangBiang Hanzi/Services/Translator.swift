//
//  Translator.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Translation

struct Translator {
    
    /// Translate a simplified chinese string into the target language.
    func translateFromSimplifiedChinese(text: String, to: Locale.Language) async throws -> String {
        return try await self.translateTo(text: text, from: Locale.Language.init(identifier: "zh-Hans"), to: to)
    }
    
    /// Translate a simplified chinese string into the target language.
    func translateFromTraditionalChinese(text: String, to: Locale.Language) async throws -> String {
        return try await self.translateTo(text: text, from: Locale.Language.init(identifier: "zh-Hant"), to: to)
    }
    
    /// Translate a string from the given language to the given language
    private func translateTo(text: String, from: Locale.Language, to: Locale.Language) async throws -> String {
        let translator = TranslationSession(installedSource: from, target: to);
        
        let translateResponse = try await translator.translate(text);
        
        return translateResponse.targetText;

    }
    
}

