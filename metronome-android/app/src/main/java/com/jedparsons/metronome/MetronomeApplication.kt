package com.jedparsons.metronome

import android.app.Application
import com.jedparsons.metronome.player.MetronomeController
import com.jedparsons.metronome.player.RealMetronomePlayer
import com.jedparsons.metronome.util.RealMetronomeClock

class MetronomeApplication : Application() {

  lateinit var appContainer: AppContainer
  private lateinit var metronomeController: MetronomeController

  init {
    System.loadLibrary("metronome")
  }

  override fun onCreate() {
    super.onCreate()

    appContainer = RealAppContainer(this)

    metronomeController = MetronomeController(
      rhythmRepository = appContainer.rhythmRepo,
      clock = RealMetronomeClock(),
      player = RealMetronomePlayer(),
      ioScope = appContainer.ioScope
    )

    registerActivityLifecycleCallbacks(metronomeController)
  }
}
