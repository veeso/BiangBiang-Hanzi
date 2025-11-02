//
//  TextModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Combine
import SwiftUI

struct TextModeView: View {
    @EnvironmentObject var settings: AppSettings

    @State private var inputText: String = ""
    @State private var pinyinText: String = ""
    @State private var translatedText: String = ""
    @State private var debounceTimer: AnyCancellable?

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {

                    // Hanzi input
                    SectionView(
                        title: "Hanzi",
                        actionLabel: "Paste",
                        actionIcon: "doc.on.clipboard"
                    ) {
                        if let pasteboard = UIPasteboard.general.string {
                            inputText = pasteboard
                        }
                    } content: {
                        TextEditor(text: $inputText)
                            .font(.title2)
                            .frame(minHeight: 120)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12).stroke(
                                    Color.secondary
                                )
                            )
                            .onChange(of: inputText) { _, _ in
                                scheduleDebouncedProcessing()
                            }
                    }

                    // Pinyin output
                    SectionView(
                        title: "Pinyin",
                        actionLabel: "Copy",
                        actionIcon: "doc.on.doc"
                    ) {
                        copyToClipboard(pinyinText, )
                    } content: {
                        TextEditor(text: .constant(pinyinText))
                            .font(.title3)
                            .frame(minHeight: 120)
                            .disabled(true)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12).stroke(
                                    Color.secondary
                                )
                            )
                    }

                    // Translation output
                    SectionView(
                        title: "Translation (\(settings.userLanguage))",
                        actionLabel: "Copy",
                        actionIcon: "doc.on.doc"
                    ) {
                        copyToClipboard(translatedText, )
                    } content: {
                        TextEditor(text: .constant(translatedText))
                            .font(.title3)
                            .frame(minHeight: 120)
                            .disabled(true)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12).stroke(
                                    Color.secondary
                                )
                            )
                    }

                    Spacer()
                }
                .padding()
            }
            .navigationTitle("Hanzi to Pinyin")
        }
    }

    private func scheduleDebouncedProcessing() {
        debounceTimer?.cancel()
        debounceTimer = Just(())
            .delay(for: .seconds(0.8), scheduler: DispatchQueue.main)
            .sink { _ in
                processInput()
            }
    }

    private func processInput() {
        guard !inputText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        else {
            pinyinText = ""
            translatedText = ""
            return
        }

        Task {
            // 1. Convert to pinyin
            pinyinText = PinyinConverter().hanziToPinyin(hanzi: inputText)
            // Translate
            do {
                let translator = Translator()
                let target = Locale.Language(identifier: settings.userLanguage)

                switch settings.chineseVariant {
                case "traditional":
                    translatedText =
                        try await translator.translateFromTraditionalChinese(
                            text: inputText,
                            to: target
                        )
                case "simplified", _:
                    // fallback to simpl
                    translatedText =
                        try await translator.translateFromSimplifiedChinese(
                            text: inputText,
                            to: target
                        )
                }
            } catch {
                translatedText =
                    "❌ Translation failed: \(error.localizedDescription)"
            }
        }
    }

    private func copyToClipboard(_ text: String, ) {
        guard !text.isEmpty else { return }
        UIPasteboard.general.string = text
    }

}

private struct SectionView<Content: View>: View {
    let title: String
    let actionLabel: String
    let actionIcon: String
    let action: () -> Void
    let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                Spacer()
                Button(action: action) {
                    Label(actionLabel, systemImage: actionIcon)
                }
            }
            content()
        }
    }
}

#Preview {
    TextModeView().environmentObject(AppSettings())
}
