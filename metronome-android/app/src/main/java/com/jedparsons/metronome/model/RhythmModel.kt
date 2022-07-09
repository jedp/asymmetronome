package com.jedparsons.metronome.model

data class RhythmModel(
  val bpm: Int = DEFAULT_BPM,
  val divisions: List<Int> = DEFAULT_DIVISIONS
) {

  companion object {
    const val DEFAULT_BPM = 120
    val DEFAULT_DIVISIONS = listOf(1, 0)
  }
}
