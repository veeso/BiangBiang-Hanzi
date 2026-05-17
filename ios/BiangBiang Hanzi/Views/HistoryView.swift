//
//  HistoryView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 17/05/2026.
//

import SwiftUI

struct HistoryView: View {
    @Environment(AppSettings.self) private var settings
    @Environment(AudioPlayerService.self) private var audio

    @State private var showTransliterated = false
    @State private var expandedIDs: Set<UUID> = []
    @State private var showClearConfirm = false
    @State private var speakingID: UUID?

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    Picker("Display", selection: $showTransliterated) {
                        Text("Original").tag(false)
                        Text("Transliterated").tag(true)
                    }
                    .pickerStyle(.segmented)

                    Button("Clear All", role: .destructive) {
                        showClearConfirm = true
                    }
                    .buttonStyle(.bordered)
                    .disabled(settings.history.isEmpty)
                }
                .padding(.horizontal)
                .padding(.vertical, 8)

                if settings.history.isEmpty {
                    Spacer()
                    ContentUnavailableView(
                        "No history yet",
                        systemImage: "clock",
                        description: Text(
                            "Saved entries from Text and Camera appear here."
                        )
                    )
                    Spacer()
                } else {
                    List {
                        ForEach(settings.history) { entry in
                            row(entry)
                        }
                        .onDelete { offsets in
                            let ids = offsets.map { settings.history[$0].id }
                            for id in ids {
                                settings.deleteHistory(id: id)
                            }
                        }
                    }
                }
            }
            .navigationTitle("History")
            .onChange(of: audio.isSpeaking) { _, speaking in
                if !speaking { speakingID = nil }
            }
            .confirmationDialog(
                "Clear all history?",
                isPresented: $showClearConfirm,
                titleVisibility: .visible
            ) {
                Button("Clear All", role: .destructive) {
                    settings.clearHistory()
                    expandedIDs.removeAll()
                }
                Button("Cancel", role: .cancel) {}
            }
        }
    }

    private func row(_ entry: HistoryEntry) -> some View {
        let text = showTransliterated
            ? entry.transliteration
            : entry.original
        let expanded = expandedIDs.contains(entry.id)
        let isThisPlaying = speakingID == entry.id && audio.isSpeaking
        return HStack(alignment: .top, spacing: 12) {
            Text(text)
                .lineLimit(expanded ? nil : 1)
                .truncationMode(.tail)
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle())
                .onTapGesture {
                    if expanded {
                        expandedIDs.remove(entry.id)
                    } else {
                        expandedIDs.insert(entry.id)
                    }
                }
            Button {
                if isThisPlaying {
                    audio.stop()
                    speakingID = nil
                } else {
                    let lang: AudioPlayerService.Language =
                        entry.variant == .cantonese
                            ? .cantonese : .mandarin
                    speakingID = entry.id
                    audio.speak(entry.original, language: lang)
                }
            } label: {
                Image(
                    systemName: isThisPlaying
                        ? "stop.circle.fill"
                        : "speaker.wave.2.fill"
                )
            }
            .buttonStyle(.borderless)
        }
        .padding(.vertical, 4)
    }
}
