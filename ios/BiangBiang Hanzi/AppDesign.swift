//
//  AppDesign.swift
//  BiangBiang Hanzi
//
//  Shared design constants used across the app for a consistent look.
//

import Foundation

enum AppDesign {
    /// Default corner radius for cards and rounded containers.
    static let cornerRadius: CGFloat = 12
    /// Larger corner radius for prominent containers (toasts, banners).
    static let cornerRadiusLarge: CGFloat = 16
    /// Compact corner radius for small surfaces (overlay labels).
    static let cornerRadiusCompact: CGFloat = 6

    /// Spacing between sections within a screen.
    static let sectionSpacing: CGFloat = 20
    /// Standard horizontal padding for content edges.
    static let horizontalPadding: CGFloat = 20
    /// Padding above the bottom safe area for floating controls.
    static let bottomToolbarPadding: CGFloat = 40
    /// Inner spacing for stacks of related controls.
    static let stackSpacing: CGFloat = 12

    /// Standard tap target size (HIG minimum).
    static let tapTarget: CGFloat = 44

    /// Default duration for short interaction animations.
    static let shortAnimation: Double = 0.2
    /// Toast fade duration.
    static let toastAnimation: Double = 0.25

    /// Standard shadow radius for floating buttons.
    static let buttonShadow: CGFloat = 4
}
