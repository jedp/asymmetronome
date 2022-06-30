//
//  MetronomeViewModel.swift
//  metronome
//
//  Created by Jed Parsons on 6/28/22.
//

import SwiftUI

/// View model for a metronome.
///
/// An interface for the underlying `MetronomeModel`.
class MetronomeViewModel: ObservableObject {
    
    @Published var data: MetronomeModel
    
    var bpm: Int { data.bpm }
    var playing: Bool { data.playing }
    var lastTap: TimeInterval = 0
    var divisions: [Int] { data.divisions }
    
    init(_ data: MetronomeModel) {
        self.data = data
    }
    
    // MARK - Intents
    
    func togglePlaying() {
        data.playing.toggle()
    }
    
    func setTempo(_ bpm: Int) {
        data.setTempo(bpm)
    }
    
    func tapToSetTempo() {
        if (lastTap == 0) {
            lastTap = ProcessInfo().systemUptime
        } else {
            let now = ProcessInfo().systemUptime
            let delta = 60 / (now - lastTap)
            // Reset if it's been a while since they tapped.
            if (delta > 10) {
                data.setTempo(Int(delta))
            }
            lastTap = now
        }
    }
    
    func setSubdivisions(group: Int, beats: Int) {
        data.setSubdivisions(group: group, beats: beats)
    }
}
