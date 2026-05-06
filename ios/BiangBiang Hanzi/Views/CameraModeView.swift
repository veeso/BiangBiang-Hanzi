//
//  CameraModeView.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import SwiftUI

struct CameraModeView: View {
    @Environment(AppSettings.self) private var settings
    @State private var cameraModel = CameraModel()

    var body: some View {
        if cameraModel.missingCameraPermission {
            CameraPermissionView()
        } else {
            CameraLiveView(cameraModel: cameraModel)
        }
    }
}

#Preview {
    CameraModeView().environment(AppSettings())
}
