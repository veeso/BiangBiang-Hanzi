//
//  TextProcessorTests.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 03/01/26.
//

import Testing

@testable import BiangBiang_Hanzi

struct TestProcessorTests {
    @Test func shouldTakeHanziFromText() throws {
        let text = TextProcessor().containsHanzi(text: "你好Pizza我爱你123")
        #expect(text)
    }

    @Test func shouldNotTakeHanziFromText() throws {
        let text = TextProcessor().containsHanzi(text: "Pizza123")
        #expect(!text)
    }

    @Test func shouldConvertHanziWordToPinyin() throws {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "你好")
        #expect(pinyin == "nǐ hǎo")
    }

    @Test func shouldConvertHanziSentenceToPinyin() throws {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "我喜欢饺子🥟")
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟")
    }

    @Test func shouldConvertTraditionalHanziSentenceToPinyin() throws {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "我喜歡餃子🥟")
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟")
    }

    @Test func shouldNotReturnTextNotContainingAnyHanzi() throws {
        let text = "This is a test string with no hanzi in it."
        let pinyin = TextProcessor().process(text: text)
        #expect(pinyin == nil)
    }

    @Test func shouldApplyAllTextTransformations() throws {
        let text = "我在NASA工作. 现在是5点."
        let expectedText = "wǒ zài NASA gōng zuò. xiàn zài shì 5 diǎn."
        let processedText = TextProcessor().process(text: text)
        #expect(processedText == expectedText)
    }

    @Test func shouldProcessSingleCharacters() throws {
        let text = "王"
        let expectedText = "wáng"
        let processedText = TextProcessor().process(text: text)
        #expect(processedText == expectedText)
    }
}
