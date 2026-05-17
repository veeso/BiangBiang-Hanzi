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

    #if DEBUG
        @State private var showResetToast = false
    #endif

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
                    .disabled(settings.isCantonese)
                } header: {
                    Label("Translation language", systemImage: "globe")
                } footer: {
                    if settings.isCantonese {
                        Text("Translation is unavailable in Cantonese mode.")
                    }
                }

                Section {
                    Picker("Variant", selection: $settings.chineseVariant) {
                        Text("Simplified").tag(AppSettings.simplifiedChinese)
                        Text("Traditional").tag(AppSettings.traditionalChinese)
                        Text("Cantonese").tag(AppSettings.cantonese)
                    }
                    .pickerStyle(.segmented)

                    if settings.isCantonese {
                        Label(
                            "Translation is disabled when Cantonese is selected. Hanzi will be romanized to Jyutping.",
                            systemImage: "info.circle"
                        )
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    }
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

                #if DEBUG
                    Section {
                        Button("Reset review prompt", systemImage: "arrow.counterclockwise") {
                            settings.reviewPromptDismissed = false
                            settings.reviewLaunchCount = 0
                            showResetToast = true
                        }
                    } header: {
                        Label("Debug", systemImage: "hammer")
                    } footer: {
                        Text("Clears dismissed flag and launch count. Restart the app to re-trigger after 3 launches.")
                    }
                #endif
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            #if DEBUG
                .overlay(alignment: .bottom) {
                    if showResetToast {
                        CopyToast(message: "Review prompt reset")
                            .padding(.bottom, 24)
                            .transition(.move(edge: .bottom).combined(with: .opacity))
                            .task {
                                try? await Task.sleep(for: .seconds(2))
                                showResetToast = false
                            }
                    }
                }
                .animation(.easeInOut, value: showResetToast)
            #endif
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
