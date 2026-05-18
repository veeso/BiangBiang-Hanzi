//
//  JyutpingDictionary.swift
//  BiangBiang Hanzi
//

import Foundation

final class JyutpingDictionary: Sendable {
    static let shared = JyutpingDictionary()

    private let table: [String: String]

    var count: Int {
        table.count
    }

    private init() {
        guard
            let url = Bundle.main.url(forResource: "cantonese", withExtension: "json"),
            let data = try? Data(contentsOf: url),
            let decoded = try? JSONDecoder().decode([String: String].self, from: data)
        else {
            assertionFailure("cantonese.json missing from bundle")
            table = [:]
            return
        }
        table = decoded
    }

    /// Returns the primary Jyutping reading for a single Han character, or nil if unknown.
    func reading(for character: String) -> String? {
        table[character]
    }
}
