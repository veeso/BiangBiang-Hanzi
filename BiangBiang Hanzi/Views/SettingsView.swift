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
            (id: id, name: Locale.current.localizedString(forIdentifier: id) ?? id)
        }
        .sorted { $0.name < $1.name }

    var body: some View {
        NavigationView {
            Form {
                Section(header: HStack {
                    Image(systemName: "globe")
                    Text("Translation language")
                }) {
                    Picker("Language", selection: $settings.userLanguage) {
                        ForEach(availableLanguages, id: \.id) { option in
                            Text(option.name).tag(option.id)
                        }
                    }
                    .pickerStyle(MenuPickerStyle())
                }

                Section(header: HStack {
                    Image(systemName: "textformat")
                    Text("Chinese variant")
                        .font(.headline)
                }) {
                    Picker("Variant", selection: $settings.chineseVariant) {
                        Text("Simplified").tag("zh-Hans")
                        Text("Traditional").tag("zh-Hant")
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline) // stile iOS classico
        }
    }
}

#Preview {
    SettingsView()
        .environmentObject(AppSettings())
}
