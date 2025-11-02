//
//  PinyinConverterTests.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Testing
@testable import BiangBiang_Hanzi

struct PinyinConverterTests {

    @Test func shouldConvertHanziWordToPinyin() throws {
        let pinyin = PinyinConverter().hanziToPinyin(hanzi: "你好");
        #expect(pinyin == "nǐ hǎo");
    }
    
    @Test func shouldConvertHanziSentenceToPinyin() throws {
        let pinyin = PinyinConverter().hanziToPinyin(hanzi: "我喜欢饺子🥟");
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟");
    }
    
    @Test func shouldConvertTraditionalHanziSentenceToPinyin() throws {
        let pinyin = PinyinConverter().hanziToPinyin(hanzi: "我喜歡餃子🥟")
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟")
    }

}
