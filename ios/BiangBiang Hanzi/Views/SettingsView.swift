//
//  SettingsView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var settings: AppSettings

    let availableLanguages: [(id: String, name: String)] =
        Locale.availableIdentifiers.map { id in
            (
                id: id,
                name: Locale.current.localizedString(forIdentifier: id) ?? id
            )
        }
        .sorted { $0.name < $1.name }

    var body: some View {
        NavigationView {
            Form {
                Section(
                    header: HStack {
                        Image(systemName: "globe")
                        Text("Translation language")
                    }
                ) {
                    Picker("Language", selection: $settings.userLanguage) {
                        ForEach(availableLanguages, id: \.id) { option in
                            Text(option.name).tag(option.id)
                        }
                    }
                    .pickerStyle(MenuPickerStyle())
                }

                Section(
                    header: HStack {
                        Image(systemName: "textformat")
                        Text("Chinese variant")
                            .font(.headline)
                    }
                ) {
                    Picker("Variant", selection: $settings.chineseVariant) {
                        Text("Simplified").tag("zh-Hans")
                        Text("Traditional").tag("zh-Hant")
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }

                Section(
                    header: HStack {
                        Image(systemName: "ladybug")
                        Text("Report a bug").font(.headline)
                    }
                ) {
                    Button {
                        openGitHubIssues()
                    } label: {
                        Label("Open a new issue on GitHub", systemImage: "link")
                    }

                    Button {
                        sendBugEmail()
                    } label: {
                        Label("Send an email", systemImage: "envelope")
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline) // stile iOS classico
        }
    }
}

func openGitHubIssues() {
    let webURL = URL(string: "https://github.com/veeso/BiangBiang-Hanzi/issues/new")!
    UIApplication.shared.open(webURL)
}

func sendBugEmail() {
    let subject = "[iOS] Bug report – BiangBiang Hanzi"
    let body = """
    Description:

    Step to reproduce:

    Device:
    iOS version:
    """
    .replacingOccurrences(of: "\n", with: "\r\n")

    let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!
    let encodedBody = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!

    let url = URL(string: "mailto:info@veeso.dev?subject=\(encodedSubject)&body=\(encodedBody)")!
    UIApplication.shared.open(url)
}

#Preview {
    SettingsView()
        .environmentObject(AppSettings())
}
