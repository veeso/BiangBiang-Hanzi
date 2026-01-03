//
//  TextProcessor.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 03/01/26.
//

import Foundation

class TextProcessor {
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
    /// 2. Take all the hanzi characters and convert to pinyin
    /// 3. add leading space to pinyin, otherwise latin characters are sticked to the other
    /// 4. trim
    func process(text: String) -> String? {
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
            let pinyin = hanziToPinyin(hanzi: hanzi)

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
                pinyin +
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

    // Given a `text` tells whether the text contains hanzi
    func containsHanzi(text: String) -> Bool {
        let range = NSRange(text.startIndex..., in: text)
        return regex.firstMatch(in: text, range: range) != nil
    }

    /// Takes a hanzi string and converts it to Pinyin notation.
    ///
    /// Example:
    ///
    /// “你好” -》 “nǐhǎo“
    /// ”我喜欢饺子🥟“ -〉 ”wǒ xǐhuān jiǎozǐ 🥟“
    func hanziToPinyin(hanzi: String) -> String {
        let mutString = NSMutableString(string: hanzi) as CFMutableString
        CFStringTransform(mutString, nil, kCFStringTransformToLatin, false)
        return mutString as String
    }
}
