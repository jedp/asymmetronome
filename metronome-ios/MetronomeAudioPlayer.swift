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
    private var lastTick: Double = 0
    private var timer: Timer?
    
    var bpm: Int
    var playing: Bool
    
    init(_ data: MetronomeModel) {
        self.data = data
        
        self.bpm = data.bpm
        self.playing = data.playing
        
        self.data.onPlayingChanged.sink(receiveValue: { playing in
            if (self.player != nil) {
                self.playing = playing
            }
        }).store(in: &cancellableBag)
        
        self.data.onBpmChanged.sink(receiveValue: {bpm in
            self.bpm = bpm
        }).store(in: &cancellableBag)
        
        initAudio()
    }
    
    func start() {
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
        if (lastTick == 0) {
            lastTick = now
        } else if ((now - lastTick) >= 60 / Double(bpm)) {
            player?.currentTime = 0
            player?.play()
            lastTick = now
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
