package com.jedparsons.metronome

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.jedparsons.metronome.repo.RhythmRepository
import com.jedparsons.metronome.storage.RhythmStore
import com.jedparsons.metronome.storage.SharedPrefsRhythmStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Dependency injection.
 */
interface AppContainer {
  val mainScope: CoroutineScope
  val rhythmRepo: RhythmRepository
}

class RealAppContainer(
  private val context: Context
) : AppContainer {

  private val sharedPrefs: SharedPreferences by lazy {
    context.getSharedPreferences("metronome-prefs", MODE_PRIVATE)
  }

  private val rhythmStore: RhythmStore by lazy {
    SharedPrefsRhythmStore(sharedPrefs)
  }

  override val mainScope: CoroutineScope by lazy {
    CoroutineScope(Dispatchers.Main)
  }

  override val rhythmRepo: RhythmRepository by lazy {
    RhythmRepository(rhythmStore)
  }
}
