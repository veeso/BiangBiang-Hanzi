//
//  AppStorage.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 02/11/25.
//

import Foundation

struct AppStorage {
    private static let chineseLangKey = "chinese"
    private static let userLangKey = "user_language"
    
    static var chineseLanguage: String {
        get { UserDefaults.standard.string(forKey: chineseLangKey) ?? "zh-Hans" } // default: simplified chinese
        set { UserDefaults.standard.set(newValue, forKey: chineseLangKey) }
    }
    
    static var userLanguage: String {
        get { UserDefaults.standard.string(forKey: userLangKey) ?? "en" } // default: inglese
        set { UserDefaults.standard.set(newValue, forKey: userLangKey) }
    }
}
