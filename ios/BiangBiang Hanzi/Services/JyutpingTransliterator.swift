//
//  JyutpingTransliterator.swift
//  BiangBiang Hanzi
//
//  Cantonese romanisation. Per-character dictionary lookup, unknown
//  characters preserved verbatim, tokens joined by single spaces
//  (faithfully the former `TextProcessor.hanziToJyutping`). Span detection,
//  passthrough and spacing are owned by the library's engine.
//

import BiangBiangUI
import Foundation

struct JyutpingTransliterator: Transliterator {
    func transliterate(_ scriptSpan: String) -> String {
        let dict = JyutpingDictionary.shared
        var tokens: [String] = []
        for scalar in scriptSpan {
            let key = String(scalar)
            if let reading = dict.reading(for: key) {
                tokens.append(reading)
            } else {
                tokens.append(key)
            }
        }
        return tokens.joined(separator: " ")
    }
}
