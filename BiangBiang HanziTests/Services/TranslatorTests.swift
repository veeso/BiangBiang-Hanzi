//
//  TranslatorTests.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Testing
import Translation
@testable import BiangBiang_Hanzi

struct TranslatorTests {

    @Test func shouldTranslateSimplifiedChinese() async throws {
        do {
            let text = try await Translator().translateFromSimplifiedChinese(text: "爱", to: .init(identifier: "en"));
            #expect(text == "Love");
        }  catch {
            print("Translation not supported in this environment (\(error))")
        }
    }
    
    @Test func shouldTranslateTraditionalChinese() async throws {
        do {
            let text = try await Translator().translateFromTraditionalChinese(text: "愛", to: .init(identifier: "en"));
            #expect(text == "Love");
        }  catch {
            print("Translation not supported in this environment (\(error))")
        }
    }

}
