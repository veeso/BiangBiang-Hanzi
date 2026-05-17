import Foundation

enum HistoryVariant: String, Codable, Equatable {
    case mandarin
    case cantonese
}

struct HistoryEntry: Identifiable, Codable, Equatable {
    let id: UUID
    let original: String
    let transliteration: String
    let variant: HistoryVariant
    let timestamp: Date

    init(
        id: UUID = UUID(),
        original: String,
        transliteration: String,
        variant: HistoryVariant,
        timestamp: Date = Date()
    ) {
        self.id = id
        self.original = original
        self.transliteration = transliteration
        self.variant = variant
        self.timestamp = timestamp
    }
}
