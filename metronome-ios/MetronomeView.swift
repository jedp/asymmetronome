//
//  MetronomeView.swift
//  metronome
//
//  Created by Jed Parsons on 6/28/22.
//

import SwiftUI

/// The UI of the metronome app
///
/// Four main regions:
///
///   - BPM display. Drag to increase or decrease by 1.
///   - Tap button. Tap to set the tempo.
///   - Subdivisions. Hold and drag numbers to create metric subdivisions.
///   - Play / Stop button.
///
/// Reacts to and sets values in the `MetronomeViewModel`.
struct MetronomeView: View {
    
    @ObservedObject var metronome: MetronomeViewModel
    
    var body: some View {
        Color.black.overlay {
            VStack(spacing: 1) {
                BeatsPerMinute(bpm: metronome.bpm, setTempo: metronome.setTempo)
                Spacer()
                TempoTapButton(tapToSetTempo: metronome.tapToSetTempo)
                Spacer()
                Subdivisions(divisions: metronome.divisions, setSubdivisions: metronome.setSubdivisions)
                Spacer()
                PlayButton(playing: metronome.playing, togglePlaying: metronome.togglePlaying)
            }
        }
        .background(.black)
        .foregroundColor(.orange)
        .padding()
    }
}

/// Bets per minute display. Drag to increase or decrease by 1.
struct BeatsPerMinute: View {
    
    let bpm: Int
    let setTempo: (Int) -> Void

    @State var currentOffset: Int = 0
    @State var lastOffset: Int = 0

    var drag: some Gesture {
        DragGesture()
            .onChanged { dragValue in
                currentOffset = Int(dragValue.location.x - dragValue.startLocation.x)
                if (abs(currentOffset - lastOffset) > 30) {
                    if (currentOffset > lastOffset) {
                        setTempo(bpm + 1)
                    } else {
                        setTempo(bpm - 1)
                    }
                    lastOffset = currentOffset
                }
            }
            .onEnded { _ in
                lastOffset = 0
            }
    }
    
    var body: some View {
        Text(String(bpm))
            .font(.system(size: 140))
            .gesture(drag)
    }
}

/// Tempo tap button. Tap to set the tempo.
struct TempoTapButton: View {
    
    let tapToSetTempo: () -> Void
    
    @State private var handledTap: Bool = false
    
    var tap: some Gesture {
        DragGesture(minimumDistance: 0.0, coordinateSpace: .global)
            .onChanged { _ in
                if (handledTap) {
                    return
                }
                tapToSetTempo()
                handledTap = true
            }
            .onEnded { _ in
                handledTap = false
            }
    }
    
    var body: some View {
        Button("TAP") {
        }
        .buttonStyle(.bordered)
        .font(.system(size: 60))
        .simultaneousGesture(tap)
    }
}

/// Subdivisions of the meter.
///
/// Renders the view model's `[Int]` field as a view builder of individial numbers
/// that can be manipulated by dragging.
struct Subdivisions: View {
    
    let divisions: [Int]
    let setSubdivisions: (Int, Int) -> Void
    
    var body: some View {
        HStack {
            ForEachEnumerated(Array(divisions[0..<divisions.count])) { i, beats in
                Subdivision(
                    beats: beats,
                    isLast: i == divisions.indices.last,
                    updateValue: { value in setSubdivisions(i, value) }
                )
            }
        }
    }
}

/// Convenience view builder for an enumerated array of items.
struct ForEachEnumerated<T, V: View>: View {
    let data: [T]
    let content: (Int, T) -> V
    
    init(_ data: [T], @ViewBuilder content: @escaping (Int, T) -> V) {
        self.data = data
        self.content = content
    }
    
    var body: some View {
        ForEach(Array(data.enumerated()), id: \.offset) { i, elem in
            content(i, elem)
        }
    }
}

/// A single subdivision of the meter
///
/// Drag to increase or decrease.
struct Subdivision: View {
    
    let beats: Int
    let isLast: Bool
    let updateValue: (Int) -> Void
    
    @State var currentOffset: Int = 0
    @State var lastOffset: Int = 0

    var drag: some Gesture {
        DragGesture()
            .onChanged { dragValue in
                currentOffset = Int(dragValue.location.x - dragValue.startLocation.x)
                if (abs(currentOffset - lastOffset) > 30) {
                    if (currentOffset > lastOffset) {
                        updateValue(beats + 1)
                    } else {
                        updateValue(beats - 1)
                    }
                    lastOffset = currentOffset
                }
            }
            .onEnded { _ in
                lastOffset = 0
            }
    }
    
    var body: some View {
        HStack {
            if (beats == 0) {
                Text(String(beats))
                    .font(.system(size: 60))
                    .foregroundColor(.gray)
            } else {
                Text(String(beats))
                    .font(.system(size: 60))
            }
            
            if (!isLast) {
                Text("+")
                    .font(.largeTitle)
                    .foregroundColor(.gray)
            }
        }
        .gesture(drag)
    }
}

/// The play button for sending the message to start or stop the metronome.
///
/// Value is stored in the underlying model so an audio player can observe changes.
struct PlayButton: View {
    
    let playing: Bool
    let togglePlaying: () -> Void
    
    var body: some View {
        Button {
            togglePlaying()
        } label: {
            Text(self.playing ? "STOP" : "PLAY")
        }
        .buttonStyle(.bordered)
        .font(.system(size: 60))
        .foregroundColor(.orange)
    }
}

struct ContentView_Previews: PreviewProvider {
    
    static var previews: some View {
        
        let metronome = MetronomeViewModel(MetronomeModel())
        
        MetronomeView(metronome: metronome)
            .preferredColorScheme(.dark)
        
        MetronomeView(metronome: metronome)
            .preferredColorScheme(.light)
    }
}
