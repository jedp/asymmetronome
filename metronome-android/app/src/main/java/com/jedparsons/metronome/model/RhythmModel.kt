package com.jedparsons.metronome.model

data class RhythmModel(

  /** Beats per minute. */
  val bpm: Int = DEFAULT_BPM,

  /**
   * Metrical subdivisions for this rhythm.
   *
   * For example, `3, 2, 2` is a 7-beat meter, broken up into three groups of 3, 2, and 2 beats.
   */
  val divisions: List<Int> = DEFAULT_DIVISIONS
) {

  companion object {
    const val DEFAULT_BPM = 120
    val DEFAULT_DIVISIONS = listOf(1, 0)
  }
}
