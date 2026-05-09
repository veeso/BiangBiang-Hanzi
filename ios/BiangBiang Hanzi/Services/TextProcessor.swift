//
//  TextProcessor.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 03/01/26.
//

import Foundation

class TextProcessor {
    enum Mode {
        case mandarin
        case cantonese
    }

    let regex: NSRegularExpression

    init() {
        // Unicode range for CJK Unified Ideographs: U+4E00–U+9FFF
        // Extended ranges (A, B, C, D...) can be added if needed.
        let pattern = "[\\u4E00-\\u9FFF]+"
        regex = try! NSRegularExpression(pattern: pattern, options: [])
    }

    /// Process given text applying the following operations:
    ///
    /// 1. if the text does not contain ANY hanzi: discard it and return.
    /// 2. Take all the hanzi characters and convert them to the chosen romanization
    ///    (Mandarin pinyin by default, or Cantonese Jyutping when `mode == .cantonese`)
    /// 3. add leading/trailing spaces around inserted romanization where appropriate
    /// 4. trim
    func process(text: String, mode: Mode = .mandarin) -> String? {
        guard containsHanzi(text: text) else { return nil }
        let range = NSRange(text.startIndex..., in: text)

        var result = text

        // We iterate matches in reverse order to avoid messing up ranges
        let matches = regex.matches(in: text, range: range).reversed()
        for match in matches {
            guard let range = Range(match.range, in: result) else {
                continue
            }

            let hanzi = String(result[range])
            let romanized: String
            switch mode {
            case .mandarin:
                romanized = hanziToPinyin(hanzi: hanzi)
            case .cantonese:
                romanized = hanziToJyutping(hanzi: hanzi)
            }

            // Space before
            let needsLeadingSpace: Bool = {
                if range.lowerBound == result.startIndex {
                    return false
                }

                let prevChar = result[result.index(before: range.lowerBound)]
                return prevChar != " " && !".,!?;:".contains(prevChar)
            }()

            // Space after
            let needsTrailingSpace: Bool = {
                guard range.upperBound < result.endIndex else {
                    return false
                }

                let nextChar = result[range.upperBound]
                return nextChar.isASCII && (nextChar.isLetter || nextChar.isNumber)
            }()

            let replacement =
                (needsLeadingSpace ? " " : "") +
                romanized +
                (needsTrailingSpace ? " " : "")

            result.replaceSubrange(range, with: replacement)
        }

        // Cleanup
        result = result.replacingOccurrences(
            of: "\\s+([.,!?;:])",
            with: "$1",
            options: .regularExpression
        )

        result = result.replacingOccurrences(
            of: "\\s{2,}",
            with: " ",
            options: .regularExpression
        )

        return result.trimmingCharacters(in: .whitespaces)
    }

    /// Given a `text` tells whether the text contains hanzi
    func containsHanzi(text: String) -> Bool {
        let range = NSRange(text.startIndex..., in: text)
        return regex.firstMatch(in: text, range: range) != nil
    }

    /// Takes a hanzi string and converts it to Pinyin notation.
    ///
    /// Example:
    ///
    /// "你好" -> "nǐhǎo"
    /// "我喜欢饺子🥟" -> "wǒ xǐhuān jiǎozǐ 🥟"
    func hanziToPinyin(hanzi: String) -> String {
        let mutString = NSMutableString(string: hanzi) as CFMutableString
        CFStringTransform(mutString, nil, kCFStringTransformToLatin, false)
        return mutString as String
    }

    /// Converts a string of Han characters to Jyutping (space-separated syllables).
    /// Unknown characters are preserved verbatim. Resulting tokens are joined by single spaces.
    func hanziToJyutping(hanzi: String) -> String {
        let dict = JyutpingDictionary.shared
        var tokens: [String] = []
        for scalar in hanzi {
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
