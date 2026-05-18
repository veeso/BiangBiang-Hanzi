//
//  TransliteratorParityTests.swift
//  BiangBiang Hanzi
//
//  Span-level parity for the two app transliterators. The library's
//  `TextProcessingEngine` owns span detection / passthrough / spacing, so
//  these assert romanisation of an already-isolated script span only —
//  the values match the former `TextProcessor` outputs.
//

@testable import BiangBiang_Hanzi
import Testing

struct PinyinTransliteratorParityTests {
    let t = PinyinTransliterator()

    @Test func word() {
        #expect(t.transliterate("你好") == "nǐ hǎo")
    }

    @Test func sentence() {
        #expect(t.transliterate("我喜欢饺子") == "wǒ xǐ huān jiǎo zǐ")
    }

    @Test func traditional() {
        #expect(t.transliterate("我喜歡餃子") == "wǒ xǐ huān jiǎo zǐ")
    }

    @Test func singleCharacter() {
        #expect(t.transliterate("王") == "wáng")
    }
}

struct JyutpingTransliteratorParityTests {
    let t = JyutpingTransliterator()

    @Test func word() {
        #expect(t.transliterate("你好") == "nei5 hou2")
    }

    @Test func sentence() {
        #expect(t.transliterate("我喜歡食餃子") == "ngo5 hei2 fun1 sik6 gaau2 zi2")
    }

    @Test func unknownCharactersPreservedVerbatim() {
        #expect(t.transliterate("A好B") == "A hou2 B")
    }
}
