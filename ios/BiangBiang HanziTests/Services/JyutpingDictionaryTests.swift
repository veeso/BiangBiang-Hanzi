//
//  JyutpingDictionaryTests.swift
//  BiangBiang Hanzi
//

@testable import BiangBiang_Hanzi
import Testing

struct JyutpingDictionaryTests {
    @Test func shouldLoadDictionaryFromBundle() {
        let dict = JyutpingDictionary.shared
        #expect(dict.count > 5000)
    }

    @Test func shouldLookupKnownCharacter() {
        #expect(JyutpingDictionary.shared.reading(for: "中") == "zung1")
        #expect(JyutpingDictionary.shared.reading(for: "字") == "zi6")
    }

    @Test func shouldReturnNilForUnknownCharacter() {
        #expect(JyutpingDictionary.shared.reading(for: "🥟") == nil)
        #expect(JyutpingDictionary.shared.reading(for: "A") == nil)
    }
}
