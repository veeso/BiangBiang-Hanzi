//
//  TextModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI
import Translation

struct TextModeView: View {
    @Environment(AppSettings.self) private var settings

    @State private var inputText: String = ""
    @State private var pinyinText: String = ""
    @State private var translatedText: String = ""
    @State private var debounceTask: Task<Void, Never>?
    @State private var translateConfig: TranslationSession.Configuration?

    private let textProcessor = TextProcessor()

    var body: some View {
        ScrollView {
            HStack(spacing: 8) {
                Image("Logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .clipShape(.rect(cornerRadius: AppDesign.cornerRadius))
                    .accessibilityHidden(true)
                Text("BiangBiang Hanzi")
                    .font(.title)
                    .bold()
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 16)

            Text("Convert Hanzi to Pinyin")
                .font(.title2)
                .padding(.vertical, 4)

            VStack(alignment: .leading, spacing: AppDesign.sectionSpacing) {
                hanziInputSection
                pinyinOutputSection
                translationSection
            }
        }
    }

    private var hanziInputSection: some View {
        SectionView(
            title: "Hanzi",
            actionLabel: "Paste",
            actionIcon: "doc.on.clipboard",
            action: pasteFromClipboard
        ) {
            TextField("Type or paste Hanzi", text: $inputText, axis: .vertical)
                .font(.title2)
                .lineLimit(5 ... 10)
                .padding(8)
                .overlay {
                    RoundedRectangle(cornerRadius: AppDesign.cornerRadius)
                        .stroke(.secondary)
                }
                .onChange(of: inputText) { _, _ in
                    scheduleDebouncedProcessing()
                }
        }
        .padding(.horizontal, AppDesign.horizontalPadding)
    }

    private var pinyinOutputSection: some View {
        SectionView(
            title: "Pinyin",
            actionLabel: "Copy",
            actionIcon: "doc.on.doc",
            action: { copyToClipboard(pinyinText) }
        ) {
            ReadOnlyTextBox(text: pinyinText, font: .title3)
        }
        .padding(.horizontal, AppDesign.horizontalPadding)
    }

    private var translationSection: some View {
        VStack(alignment: .leading, spacing: AppDesign.stackSpacing) {
            SectionView(
                title: "Translation",
                actionLabel: "Copy",
                actionIcon: "doc.on.doc",
                action: { copyToClipboard(translatedText) }
            ) {
                ReadOnlyTextBox(text: translatedText, font: .title3)
            }

            HStack {
                Spacer()
                Button("Translate", systemImage: "globe", action: triggerTranslation)
                    .buttonStyle(.borderedProminent)
                    .buttonBorderShape(.capsule)
                    .font(.headline)
                    .translationTask(translateConfig) { session in
                        await runTranslation(using: session)
                    }
                    .accessibilityHint("Translate the Hanzi text to your selected language")
            }
        }
        .padding(.horizontal, AppDesign.horizontalPadding)
    }

    private func triggerTranslation() {
        guard translateConfig == nil else {
            translateConfig?.invalidate()
            return
        }

        translateConfig = .init(
            source: .init(identifier: settings.chineseVariant),
            target: .init(identifier: settings.userLanguage)
        )
    }

    private func runTranslation(using session: TranslationSession) async {
        do {
            let response = try await session.translate(inputText)
            translatedText = response.targetText
        } catch {
            translatedText = "❌ Translation failed: \(error.localizedDescription)"
        }
    }

    private func scheduleDebouncedProcessing() {
        debounceTask?.cancel()
        debounceTask = Task {
            try? await Task.sleep(for: .milliseconds(800))
            guard !Task.isCancelled else { return }
            processInput()
        }
    }

    private func processInput() {
        let trimmed = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            pinyinText = ""
            translatedText = ""
            return
        }
        pinyinText = textProcessor.process(text: inputText) ?? ""
    }

    private func pasteFromClipboard() {
        if let pasteboard = UIPasteboard.general.string {
            inputText = pasteboard
        }
    }

    private func copyToClipboard(_ text: String) {
        guard !text.isEmpty else { return }
        UIPasteboard.general.string = text
    }
}

private struct ReadOnlyTextBox: View {
    let text: String
    let font: Font

    var body: some View {
        Text(text)
            .font(font)
            .frame(maxWidth: .infinity, minHeight: 120, alignment: .topLeading)
            .padding(8)
            .textSelection(.enabled)
            .overlay {
                RoundedRectangle(cornerRadius: AppDesign.cornerRadius)
                    .stroke(.secondary)
            }
    }
}

#Preview {
    TextModeView().environment(AppSettings())
}
