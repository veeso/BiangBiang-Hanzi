//
//  AudioPlayerService.swift
//  BiangBiang Hanzi
//
//  Created by christian visintin on 16/05/2026.
//

import AVFoundation
import Foundation
import Observation

@MainActor
@Observable
final class AudioPlayerService: NSObject {
    enum State: Equatable {
        case idle
        case speaking
    }

    enum Language {
        case mandarin
        case cantonese

        /// BCP-47 language tag passed to `AVSpeechSynthesisVoice`.
        var voiceIdentifier: String {
            switch self {
            case .mandarin: "zh-CN"
            case .cantonese: "zh-HK"
            }
        }
    }

    private(set) var state: State = .idle

    private let synthesizer = AVSpeechSynthesizer()
    private var player: AVPlayer?
    private var endObserver: NSObjectProtocol?
    private var statusObservation: NSKeyValueObservation?

    override init() {
        super.init()
        synthesizer.delegate = self
    }

    /// Speaks `text` aloud using the system speech synthesizer.
    ///
    /// Any in-progress playback is stopped first. Empty or whitespace-only
    /// input is ignored. On success, `state` transitions to `.speaking` and
    /// returns to `.idle` when the utterance finishes or is cancelled.
    ///
    /// - Parameters:
    ///   - text: The text to synthesize.
    ///   - language: Chinese variant selecting the synthesis voice.
    func speak(_ text: String, language: Language) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        stop()
        configureSession()
        let utterance = AVSpeechUtterance(string: trimmed)
        utterance.voice = AVSpeechSynthesisVoice(language: language.voiceIdentifier)
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        state = .speaking
        synthesizer.speak(utterance)
    }

    /// Stops any active speech or audio playback and resets `state` to `.idle`.
    ///
    /// Safe to call when nothing is playing. Tears down the player, KVO
    /// observation, and end-of-playback notification observer.
    func stop() {
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }
        player?.pause()
        player = nil
        statusObservation?.invalidate()
        statusObservation = nil
        if let observer = endObserver {
            NotificationCenter.default.removeObserver(observer)
            endObserver = nil
        }
        state = .idle
    }

    /// Activates the shared audio session for spoken-audio playback (iOS only).
    private func configureSession() {
        #if os(iOS)
            let session = AVAudioSession.sharedInstance()
            try? session.setCategory(.playback, mode: .spokenAudio, options: [])
            try? session.setActive(true, options: [])
        #endif
    }

    /// `true` while an utterance is actively being spoken.
    var isSpeaking: Bool {
        state == .speaking
    }
}

extension AudioPlayerService: AVSpeechSynthesizerDelegate {
    nonisolated func speechSynthesizer(_: AVSpeechSynthesizer, didFinish _: AVSpeechUtterance) {
        Task { @MainActor in
            if case .speaking = state { state = .idle }
        }
    }

    nonisolated func speechSynthesizer(_: AVSpeechSynthesizer, didCancel _: AVSpeechUtterance) {
        Task { @MainActor in
            if case .speaking = state { state = .idle }
        }
    }
}
