//
//  CameraPermissionView.swift
//  BiangBiang Hanzi
//
//  Shown when camera permission is denied or restricted.
//

import SwiftUI
import UIKit

struct CameraPermissionView: View {
    @Environment(\.openURL) private var openURL

    var body: some View {
        ContentUnavailableView {
            Label("Camera access disabled", systemImage: "camera.slash")
        } description: {
            Text("Enable camera permissions in Settings to scan Hanzi from the live feed.")
        } actions: {
            Button("Open Settings", action: openSystemSettings)
                .buttonStyle(.borderedProminent)
        }
    }

    private func openSystemSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString) else {
            return
        }
        openURL(url)
    }
}

#Preview {
    CameraPermissionView()
}
