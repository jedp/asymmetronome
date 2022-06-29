//
//  MetronomeModel.swift
//  metronome
//
//  Created by Jed Parsons on 6/28/22.
//

import Foundation
import Combine

/// Model for a metronome.
///
/// This metronome is better than other metronomes because it can subdivide a rythym
/// into groups of beats, stressing the first beat of each group on playback.
///
/// This is useful for practicing pieces by, say, Bartok, who might specify a meter that should
/// be felt as 4 + 2 + 3.
struct MetronomeModel {
    
    /// Beats per minute.
    private(set) var bpm: Int = 120 {
        didSet {
            onBpmChanged.send(bpm)
        }
    }
    /// Rhythmic subdivisions.
    ///
    /// An array of ints, where each int represents the number of beats in the subdivision.
    private(set) var divisions: [Int] = [1, 0] {
        didSet {
            onDivisionsChanged.send(divisions)
        }
    }
    /// Whether the metronome should be playing or silent.
    var playing: Bool = false {
        didSet {
            onPlayingChanged.send(playing)
        }
    }
    
    /// Observable value of `playing`.
    var onPlayingChanged = PassthroughSubject<Bool, Never>()
    /// Observable value of `bpm`.
    var onBpmChanged = PassthroughSubject<Int, Never>()
    /// Observable value of `divisions`.
    var onDivisionsChanged = PassthroughSubject<[Int], Never>()
    
    /// Set the tempo in beats per minute.
    mutating func setTempo(_ bpm: Int) {
        self.bpm = bpm.clamp(30...350)
    }

    /// Set the subdivisions of the specified `group` to the given number of `beats`.
    mutating func setSubdivisions(group: Int, beats: Int) {
        if (group < 0 || group >= divisions.count) {
            return
        }
        
        var newDivisions = divisions
        
        // First group must always have at least one beat.
        newDivisions[group] = beats.clamp((group == 0 ? 1 : 0)...16)
        
        // Remove any subdivisions after the first 0.
        if let i = newDivisions.firstIndex(where: { $0 == 0 }) {
            newDivisions = Array(newDivisions[0...i])
        }
        
        // Maybe add another 0.
        if (newDivisions.indices.count < 4) {
            if (newDivisions.last != 0) {
                newDivisions.append(0)
            }
        }
        
        divisions = newDivisions
    }
}

extension Comparable {
    /// Clamp a range between two values.
    func clamp(_ range: ClosedRange<Self>) -> Self {
        min(max(self, range.lowerBound), range.upperBound)
    }
}

