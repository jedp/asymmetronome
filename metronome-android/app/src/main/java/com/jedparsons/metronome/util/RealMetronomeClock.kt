package com.jedparsons.metronome.util

class RealMetronomeClock : MetronomeClock {

  override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
