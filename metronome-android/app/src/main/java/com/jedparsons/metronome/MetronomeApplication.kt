package com.jedparsons.metronome

import android.app.Application
import com.jedparsons.metronome.player.MetronomeController

class MetronomeApplication : Application() {

  lateinit var appContainer: AppContainer
  private lateinit var metronomeController: MetronomeController

  override fun onCreate() {
    super.onCreate()

    appContainer = RealAppContainer(this)

    metronomeController = MetronomeController(appContainer.rhythmRepo, assets)

    registerActivityLifecycleCallbacks(metronomeController)
  }
}
