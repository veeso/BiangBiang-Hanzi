//
//  HanziExtractorTests.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Testing
import Translation

@testable import BiangBiang_Hanzi

struct HanziExtractorTests {
    @Test func shouldTakeHanziFromText() throws {
        let text = HanziExtractor().extract(text: "你好Pizza我爱你")
        #expect(text == "你好我爱你")
    }

    @Test func shouldNotTakeHanziFromText() throws {
        let text = HanziExtractor().extract(text: "Pizza123")
        #expect(text == nil)
    }
}
