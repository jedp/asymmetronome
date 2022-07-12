package com.jedparsons.metronome.repo

import com.jedparsons.metronome.model.RhythmModel
import com.jedparsons.metronome.repo.RhythmData.Loading
import com.jedparsons.metronome.repo.RhythmData.Updated
import com.jedparsons.metronome.storage.RhythmStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for observing and modifying all state.
 *
 * Doesn't save until you call [save], so do that when your livecycle ends.
 */
class RhythmRepository(
  private val rhythmStore: RhythmStore
) {

  private val _rhythm = MutableStateFlow<RhythmData>(Loading)
  private val _playing = MutableStateFlow(false)

  /** Current Play/pause state. */
  val playing: StateFlow<Boolean> = _playing

  /** Current rhythm. */
  val rhythm: StateFlow<RhythmData> = _rhythm

  /** Load the previously-saved rhythm data and emit it. */
  suspend fun load() {
    _rhythm.value = Updated(rhythmStore.load())
  }

  /** Save the current rhythm data. */
  suspend fun save() {
    (_rhythm.value as? Updated)?.let {
      rhythmStore.save(it.rhythmModel)
    }
  }

  /** Set the playing state. */
  fun setPlaying(playing: Boolean) {
    _playing.value = playing
  }

  /** Set the tempo in beats per minute. */
  fun setBpm(bpm: Int) {
    (_rhythm.value as? Updated)?.let {
      _rhythm.value = Updated(
        it.rhythmModel.copy(bpm = bpm)
      )
    }
  }

  /** Set the metrical subdivisions. */
  fun setDivisions(divisions: List<Int>) {
    (_rhythm.value as? Updated)?.let {
      _rhythm.value = Updated(
        it.rhythmModel.copy(divisions = divisions)
      )
    }
  }
}

sealed class RhythmData {
  object Loading : RhythmData()
  data class Updated(
    val rhythmModel: RhythmModel
  ) : RhythmData()
}
