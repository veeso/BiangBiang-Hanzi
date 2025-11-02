//
//  HanziExtractor.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Foundation

struct HanziExtractor {

    /// Given a string takes out hanzi text
    func extract(text: String) -> String? {
        // Unicode range for CJK Unified Ideographs: U+4E00–U+9FFF
        // Extended ranges (A, B, C, D...) can be added if needed.
        let pattern = "[\\u4E00-\\u9FFF]+"

        guard
            let regex = try? NSRegularExpression(pattern: pattern, options: [])
        else {
            return ""
        }

        let range = NSRange(text.startIndex..<text.endIndex, in: text)
        let matches = regex.matches(in: text, options: [], range: range)

        let hanzi = matches.compactMap {
            Range($0.range, in: text).map { String(text[$0]) }
        }.joined()

        if hanzi.isEmpty {
            return nil
        } else {
            return hanzi
        }
    }

}
