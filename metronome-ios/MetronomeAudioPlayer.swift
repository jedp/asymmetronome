//
//  MetronomeAudioPlayer.swift
//  metronome
//
//  Created by Jed Parsons on 6/28/22.
//

import Foundation
import AVFoundation
import Combine

/// Metronome audio player
protocol MetronomePlayer {
    
    /// Enable metronome.
    func start()
    
    /// Disable metronome.
    func stop()
}

/// Real metronome audio player.
///
/// Observes the `MetronomeModel` and plays audio according to the tempo and
/// metrical subdivisions.
///
/// TODO: Metrical stress for subdivisions.
class RealMetronomePlayer: MetronomePlayer, ObservableObject {
    
    private var cancellableBag = Set<AnyCancellable>()
    
    @Published var data: MetronomeModel
    
    private var player: AVAudioPlayer?
    private var cancellable: AnyCancellable?
    private var lastBeat: Double = 0
    private var emphasisPattern: [Int] = [1]
    private var currentEmphasis: Int = 0
    private var timer: Timer?
    
    var bpm: Double // Store as a double to avoid casting on every use.
    var playing: Bool
    
    init(_ data: MetronomeModel) {
        self.data = data
        
        self.bpm = Double(data.bpm)
        self.playing = data.playing
        
        self.data.onPlayingChanged.sink(receiveValue: { playing in
            self.playing = playing
        }).store(in: &cancellableBag)
        
        self.data.onBpmChanged.sink(receiveValue: {bpm in
            self.bpm = Double(bpm)
        }).store(in: &cancellableBag)

        self.data.onDivisionsChanged.sink(receiveValue: {divisions in
            var newPattern: [Int] = []
            divisions
                .filter{$0 > 0}
                .forEach { value in
                    for i in 0..<value {
                        newPattern.append((i == 0) ? 1 : 0)
                    }
                }
            self.emphasisPattern = newPattern
        }).store(in: &cancellableBag)
    }
    
    deinit {
        for cancellable in cancellableBag {
            cancellable.cancel()
        }
    }

    func start() {
        initAudio()
        timer = Timer.scheduledTimer(withTimeInterval: 0.001, repeats: true) {_ in
            if (self.playing) {
                self.tick()
            }
        }
    }
    
    func stop() {
        timer = nil
    }
    
    private func tick() {
        let now = Date().timeIntervalSince1970
        if (lastBeat == 0) {
            lastBeat = now
        } else if ((now - lastBeat) >= (60 / bpm)) {
            player?.currentTime = 0
            player?.play()
            lastBeat = now

            // Stress the beat according the the emphasis pattern.
            if (currentEmphasis >= emphasisPattern.count) {
                currentEmphasis = 0
            }
            player?.volume = (emphasisPattern[currentEmphasis] == 1) ? 1 : 0.2
            currentEmphasis += 1
        }
    }
    
    private func initAudio() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            guard let metronomeSound = Bundle.main.url(forResource: "HandDrum", withExtension: "wav") else {
                print("Audio file not found.")
                return
            }
            let player = try AVAudioPlayer (contentsOf: metronomeSound)
            self.player = player
            print("Audio ready.")
        } catch let error {
            print("Arr narr Cliarr! \(error.localizedDescription)")
        }
    }
}

class FakeMetronomePlayer: MetronomePlayer {
    func start() {
        print("Metronome: start")
    }
    
    func stop() {
        print("Metronome: stop")
    }
}
