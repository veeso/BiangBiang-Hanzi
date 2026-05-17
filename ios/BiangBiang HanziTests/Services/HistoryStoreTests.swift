@testable import BiangBiang_Hanzi
import Testing

struct HistoryStoreTests {
    private func entry(
        _ original: String,
        _ variant: HistoryVariant = .mandarin
    ) -> HistoryEntry {
        HistoryEntry(
            original: original,
            transliteration: original + "-t",
            variant: variant
        )
    }

    @Test func insertPrependsNewest() {
        var list: [HistoryEntry] = []
        list = HistoryStore.insert(entry("A"), into: list)
        list = HistoryStore.insert(entry("B"), into: list)
        #expect(list.map(\.original) == ["B", "A"])
    }

    @Test func skipsConsecutiveDuplicateSameVariant() {
        var list: [HistoryEntry] = []
        list = HistoryStore.insert(entry("A"), into: list)
        list = HistoryStore.insert(entry("A"), into: list)
        #expect(list.count == 1)
    }

    @Test func keepsSameTextDifferentVariant() {
        var list: [HistoryEntry] = []
        list = HistoryStore.insert(entry("A", .mandarin), into: list)
        list = HistoryStore.insert(entry("A", .cantonese), into: list)
        #expect(list.count == 2)
    }

    @Test func capEvictsOldestBeyond500() {
        var list: [HistoryEntry] = []
        for i in 0 ..< 520 {
            list = HistoryStore.insert(entry("E\(i)"), into: list)
        }
        #expect(list.count == 500)
        #expect(list.first?.original == "E519")
        #expect(list.last?.original == "E20")
    }

    @Test func deleteRemovesById() {
        let a = entry("A")
        var list = HistoryStore.insert(a, into: [])
        list = HistoryStore.delete(id: a.id, from: list)
        #expect(list.isEmpty)
    }

    @Test func clearEmptiesList() {
        var list = HistoryStore.insert(entry("A"), into: [])
        list = HistoryStore.clear()
        #expect(list.isEmpty)
    }
}
