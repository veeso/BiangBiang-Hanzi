//
//  CopyToast.swift
//  BiangBiang Hanzi
//
//  Brief confirmation toast shown after copying recognized text.
//

import SwiftUI

struct CopyToast: View {
    var body: some View {
        Text("Text copied")
            .font(.subheadline.weight(.semibold))
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(.ultraThinMaterial)
            .clipShape(.rect(cornerRadius: AppDesign.cornerRadius))
            .shadow(radius: 6)
    }
}

#Preview {
    CopyToast()
}
