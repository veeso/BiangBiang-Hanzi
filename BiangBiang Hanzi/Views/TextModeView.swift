//
//  TextModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Combine
import SwiftUI
import Translation

struct TextModeView: View {
    @EnvironmentObject var settings: AppSettings

    @State private var inputText: String = ""
    @State private var pinyinText: String = ""
    @State private var translatedText: String = ""
    @State private var debounceTimer: AnyCancellable?
    @State private var translateConfig: TranslationSession.Configuration?

    var body: some View {
        NavigationView {
            ScrollView {
                HStack(spacing: 8) {
                    Image("Logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 48, height: 48)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    Text("BiangBiang Hanzi")
                        .font(.title)
                        .fontWeight(.semibold)
                        .foregroundColor(.black)
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 16)

                HStack(spacing: 8) {
                    Text("Convert Hanzi to Pinyin")
                        .font(.title2)
                        .foregroundColor(.black)
                }
                .padding(.vertical, 4)

                VStack(alignment: .leading, spacing: 20) {
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
                    }.padding(.horizontal, 20)

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
                    }.padding(.horizontal, 20)

                    VStack(alignment: .leading, spacing: 12) {
                        // Translation output
                        SectionView(
                            title: "Translation",
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

                        HStack {
                            Spacer()  // push button to right

                            Button {
                                Task { triggerTranslation() }
                            } label: {
                                Label("Translate", systemImage: "globe")
                                    .font(.headline)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(Color.accentColor)
                                    .foregroundColor(.white)
                                    .clipShape(Capsule())

                            }
                            .translationTask(translateConfig) { session in
                                do {
                                    // Use the session the task provides to translate the text.
                                    let response = try await session.translate(
                                        inputText
                                    )
                                    // Update the view with the translated result.
                                    translatedText = response.targetText
                                } catch {
                                    translatedText =
                                        "❌ Translation failed: \(error.localizedDescription)"
                                }
                            }
                            .help("Translate")
                        }

                    }
                    .padding(.horizontal, 20)
                }
            }
        }
    }

    private func triggerTranslation() {
        guard translateConfig == nil else {
            // Call .invalidate() method to trigger the translation again
            // with the same configuration instance
            translateConfig?.invalidate()
            return
        }

        // Create a new configuration for the translation session.
        // This configuration will target the user language as the target language.
        translateConfig = .init(
            source: .init(identifier: settings.chineseVariant),
            target: .init(identifier: settings.userLanguage)
        )
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
