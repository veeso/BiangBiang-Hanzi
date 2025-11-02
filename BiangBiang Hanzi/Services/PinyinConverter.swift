//
//  PinyinConverter.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Foundation

struct PinyinConverter {
    
    /// Takes a hanzi string and converts it to Pinyin notation.
    ///
    /// Example:
    ///
    /// “你好” -》 “nǐhǎo“
    /// ”我喜欢饺子🥟“ -〉 ”wǒ xǐhuān jiǎozǐ 🥟“
    func hanziToPinyin(hanzi: String) -> String {
        let mutString = NSMutableString(string: hanzi) as CFMutableString;
        CFStringTransform(mutString, nil, kCFStringTransformToLatin, false);
        return mutString as String
    }
    
}
