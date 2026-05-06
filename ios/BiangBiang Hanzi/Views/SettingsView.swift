//
//  SettingsView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI

struct SettingsView: View {
    @Environment(AppSettings.self) private var settings
    @Environment(\.openURL) private var openURL

    private static let availableLanguages: [(id: String, name: String)] =
        Locale.availableIdentifiers.map { id in
            (
                id: id,
                name: Locale.current.localizedString(forIdentifier: id) ?? id
            )
        }
        .sorted { $0.name < $1.name }

    var body: some View {
        @Bindable var settings = settings

        NavigationStack {
            Form {
                Section {
                    Picker("Language", selection: $settings.userLanguage) {
                        ForEach(Self.availableLanguages, id: \.id) { option in
                            Text(option.name).tag(option.id)
                        }
                    }
                    .pickerStyle(.menu)
                } header: {
                    Label("Translation language", systemImage: "globe")
                }

                Section {
                    Picker("Variant", selection: $settings.chineseVariant) {
                        Text("Simplified").tag("zh-Hans")
                        Text("Traditional").tag("zh-Hant")
                    }
                    .pickerStyle(.segmented)
                } header: {
                    Label("Chinese variant", systemImage: "textformat")
                }

                Section {
                    Button(
                        "Open a new issue on GitHub",
                        systemImage: "link",
                        action: openGitHubIssues
                    )
                    Button(
                        "Send an email",
                        systemImage: "envelope",
                        action: sendBugEmail
                    )
                } header: {
                    Label("Report a bug", systemImage: "ladybug")
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func openGitHubIssues() {
        guard let url = URL(string: "https://github.com/veeso/BiangBiang-Hanzi/issues/new") else {
            return
        }
        openURL(url)
    }

    private func sendBugEmail() {
        let subject = "[iOS] Bug report – BiangBiang Hanzi"
        let body = """
        Description:

        Step to reproduce:

        Device:
        iOS version:
        """
        .replacingOccurrences(of: "\n", with: "\r\n")

        guard
            let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
            let encodedBody = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
            let url = URL(string: "mailto:info@veeso.dev?subject=\(encodedSubject)&body=\(encodedBody)")
        else { return }
        openURL(url)
    }
}

#Preview {
    SettingsView()
        .environment(AppSettings())
}
