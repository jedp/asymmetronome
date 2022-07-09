package com.jedparsons.metronome

import android.app.Application

class MetronomeApplication : Application() {

  lateinit var appContainer: AppContainer

  override fun onCreate() {
    super.onCreate()

    appContainer = RealAppContainer(this)
  }
}