//
//  PinyinTransliterator.swift
//  BiangBiang Hanzi
//
//  Mandarin romanisation. The library's `TextProcessingEngine` owns span
//  detection, passthrough and spacing; this only romanises one isolated
//  script span via a single `CFStringTransform` to Latin (faithfully the
//  former `TextProcessor.hanziToPinyin`).
//

import BiangBiangUI
import Foundation

struct PinyinTransliterator: Transliterator {
    func transliterate(_ scriptSpan: String) -> String {
        let mutString = NSMutableString(string: scriptSpan) as CFMutableString
        CFStringTransform(mutString, nil, kCFStringTransformToLatin, false)
        return mutString as String
    }
}
