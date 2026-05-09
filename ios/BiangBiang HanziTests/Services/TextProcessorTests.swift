//
//  TextProcessorTests.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 03/01/26.
//

@testable import BiangBiang_Hanzi
import Testing

struct TestProcessorTests {
    @Test func shouldTakeHanziFromText() {
        let text = TextProcessor().containsHanzi(text: "你好Pizza我爱你123")
        #expect(text)
    }

    @Test func shouldNotTakeHanziFromText() {
        let text = TextProcessor().containsHanzi(text: "Pizza123")
        #expect(!text)
    }

    @Test func shouldConvertHanziWordToPinyin() {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "你好")
        #expect(pinyin == "nǐ hǎo")
    }

    @Test func shouldConvertHanziSentenceToPinyin() {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "我喜欢饺子🥟")
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟")
    }

    @Test func shouldConvertTraditionalHanziSentenceToPinyin() {
        let pinyin = TextProcessor().hanziToPinyin(hanzi: "我喜歡餃子🥟")
        #expect(pinyin == "wǒ xǐ huān jiǎo zǐ🥟")
    }

    @Test func shouldNotReturnTextNotContainingAnyHanzi() {
        let text = "This is a test string with no hanzi in it."
        let pinyin = TextProcessor().process(text: text)
        #expect(pinyin == nil)
    }

    @Test func shouldApplyAllTextTransformations() {
        let text = "我在NASA工作. 现在是5点."
        let expectedText = "wǒ zài NASA gōng zuò. xiàn zài shì 5 diǎn."
        let processedText = TextProcessor().process(text: text)
        #expect(processedText == expectedText)
    }

    @Test func shouldProcessSingleCharacters() {
        let text = "王"
        let expectedText = "wáng"
        let processedText = TextProcessor().process(text: text)
        #expect(processedText == expectedText)
    }

    @Test func shouldConvertHanziWordToJyutping() {
        let jyutping = TextProcessor().hanziToJyutping(hanzi: "你好")
        #expect(jyutping == "nei5 hou2")
    }

    @Test func shouldConvertHanziSentenceToJyutpingPreservingPunctuation() {
        let text = "我喜歡食餃子🥟"
        let result = TextProcessor().process(text: text, mode: .cantonese)
        #expect(result == "ngo5 hei2 fun1 sik6 gaau2 zi2🥟")
    }

    @Test func shouldKeepUnknownCharsVerbatimInCantoneseMode() {
        let result = TextProcessor().process(text: "A好B", mode: .cantonese)
        #expect(result == "A hou2 B")
    }

    @Test func shouldDefaultToMandarinMode() {
        let result = TextProcessor().process(text: "你好")
        #expect(result == "nǐ hǎo")
    }
}
