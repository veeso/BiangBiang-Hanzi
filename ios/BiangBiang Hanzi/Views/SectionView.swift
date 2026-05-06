//
//  SectionView.swift
//  BiangBiang Hanzi
//
//  Reusable titled section with a single trailing action button.
//

import SwiftUI

struct SectionView<Content: View>: View {
    let title: String
    let actionLabel: String
    let actionIcon: String
    let action: () -> Void
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                Spacer()
                Button(actionLabel, systemImage: actionIcon, action: action)
            }
            content
        }
    }
}
