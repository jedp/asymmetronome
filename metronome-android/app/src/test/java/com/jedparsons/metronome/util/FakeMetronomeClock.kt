package com.jedparsons.metronome.util

class FakeMetronomeClock: MetronomeClock {

  private var currentTime: Long = 0

  override fun currentTimeMillis(): Long  = currentTime

  fun advanceClock(advanceBy: Long) {
    currentTime += advanceBy
  }
}
