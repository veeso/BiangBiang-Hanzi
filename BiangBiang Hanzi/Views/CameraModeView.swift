//
//  CameraModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI

struct CameraModeView: View {
    @EnvironmentObject var settings: AppSettings

    var body: some View {
        Text("Camera mode")
            .font(.title)
            .padding()
    }
}

#Preview {
    CameraModeView().environmentObject(AppSettings())
}
