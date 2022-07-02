//
//  MetronomeApp.swift
//  metronome
//
//  Created by Jed Parsons on 6/28/22.
//

import SwiftUI

@main
struct metronomeApp: App {
    
    private var metronomeModel: MetronomeModel
    private var metronomeViewModel: MetronomeViewModel
    private var metronomePlayer: MetronomePlayer
    
    init() {
        metronomeModel = MetronomeModel()
        metronomeViewModel = MetronomeViewModel(metronomeModel)
        metronomePlayer = RealMetronomePlayer(metronomeModel)
    }
    
    var body: some Scene {
        WindowGroup {
            MetronomeView(metronome: metronomeViewModel)
                .onAppear {
                    metronomePlayer.start()
                }
        }
    }
}
