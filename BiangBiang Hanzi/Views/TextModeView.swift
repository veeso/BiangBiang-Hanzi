//
//  TextModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI

struct TextModeView: View {
    @EnvironmentObject var settings: AppSettings

    var body: some View {
        Text("Hello, world!")
            .font(.title)
            .padding()
    }
}

#Preview {
    TextModeView().environmentObject(AppSettings())
}
