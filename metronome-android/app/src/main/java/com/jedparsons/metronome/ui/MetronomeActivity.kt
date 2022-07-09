package com.jedparsons.metronome.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.jedparsons.metronome.AppContainer
import com.jedparsons.metronome.MetronomeApplication
import com.jedparsons.metronome.player.MetronomeController
import com.jedparsons.metronome.player.MetronomePlayer
import com.jedparsons.metronome.repo.RhythmData.Updated
import com.jedparsons.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch

class MetronomeActivity : ComponentActivity() {

  private val beatsPerMinuteViewModel: BeatsPerMinuteViewModel by viewModels()
  private val playButtonViewModel: PlayButtonViewModel by viewModels()
  private val subdivisionsViewModel: SubdivisionsViewModel by viewModels()

  private val metronomePlayer = MetronomePlayer()
  private lateinit var metronomeController: MetronomeController
  private lateinit var app: AppContainer

  init {
    System.loadLibrary("metronome")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    app = (application as MetronomeApplication).appContainer

    metronomeController = MetronomeController(app.rhythmRepo, metronomePlayer)

    setContent {
      MetronomeTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colors.background,
          contentColor = MaterialTheme.colors.primary
        ) {
          MetronomeScreen(
            beatsPerMinuteViewModel,
            playButtonViewModel,
            subdivisionsViewModel
          )
        }
      }
    }

    app.mainScope.launch {
      app.rhythmRepo.rhythm.collect {
        (it as? Updated)?.rhythmModel?.let { model ->
          beatsPerMinuteViewModel.updateBPM(model.bpm)
          subdivisionsViewModel.updateValues(model.divisions)
        }
      }
    }

    beatsPerMinuteViewModel.bpm.observe(this) {
      app.rhythmRepo.setBpm(it)
    }
    playButtonViewModel.playing.observe(this) { play ->
      app.rhythmRepo.setPlaying(play)
    }
    subdivisionsViewModel.values.observe(this) {
      app.rhythmRepo.setDivisions(it)
    }

    window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun onStart() {
    super.onStart()

    metronomePlayer.setupAudioStream()
    metronomePlayer.loadWavAssets(assets)
    metronomePlayer.startAudioStream()
    Log.i(TAG, "Prepared audio stream")
  }

  override fun onResume() {
    super.onResume()

    restoreState()
  }

  override fun onPause() {
    saveState()

    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()

    metronomePlayer.teardownAudioStream()
    metronomePlayer.unloadWavAssets()
    Log.i(TAG, "Cleaned up audio stream")
  }

  private fun saveState() {
    app.mainScope.launch {
      app.rhythmRepo.save()
    }
  }

  private fun restoreState() {
    app.mainScope.launch {
      app.rhythmRepo.load()
    }
  }

  companion object {
    const val TAG = "MetronomeActivity"
  }
}