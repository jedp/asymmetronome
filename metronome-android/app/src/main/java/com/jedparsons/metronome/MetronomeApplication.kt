package com.jedparsons.metronome

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.jedparsons.metronome.player.MetronomeController
import com.jedparsons.metronome.player.MetronomePlayer

class MetronomeApplication : Application() {

  lateinit var appContainer: AppContainer
  lateinit var metronomeController: MetronomeController

  override fun onCreate() {
    super.onCreate()

    appContainer = RealAppContainer(this)

    metronomeController = MetronomeController(appContainer.rhythmRepo, assets, MetronomePlayer())

    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

      override fun onActivityStarted(activity: Activity) = metronomeController.startService()

      override fun onActivityStopped(activity: Activity) = metronomeController.stopService()

      override fun onActivityResumed(activity: Activity) = Unit

      override fun onActivityPaused(activity: Activity) = Unit

      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

      override fun onActivityDestroyed(activity: Activity) = Unit
    })
  }
}
