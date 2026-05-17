import Foundation

/// Pure history-list mutation rules. No persistence, no UI.
enum HistoryStore {
    /// Silent safety cap. Never surfaced in UI.
    static let safetyCap = 500

    /// Prepend `entry` (newest first), skipping if it duplicates the
    /// most-recent entry by original text + variant. Evicts oldest entries
    /// beyond `safetyCap`.
    static func insert(
        _ entry: HistoryEntry,
        into list: [HistoryEntry]
    ) -> [HistoryEntry] {
        if let newest = list.first,
           newest.original == entry.original,
           newest.variant == entry.variant
        {
            return list
        }
        var result = list
        result.insert(entry, at: 0)
        if result.count > safetyCap {
            result.removeLast(result.count - safetyCap)
        }
        return result
    }

    static func delete(
        id: UUID,
        from list: [HistoryEntry]
    ) -> [HistoryEntry] {
        list.filter { $0.id != id }
    }

    static func clear() -> [HistoryEntry] {
        []
    }
}
