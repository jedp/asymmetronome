package com.jedparsons.metronome.repo

import com.jedparsons.metronome.model.RhythmModel
import com.jedparsons.metronome.repo.RhythmData.Loading
import com.jedparsons.metronome.repo.RhythmData.Updated
import com.jedparsons.metronome.storage.RhythmStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RhythmRepository(
  private val rhythmStore: RhythmStore
) {

  private val _rhythm = MutableStateFlow<RhythmData>(Loading)
  private val _playing = MutableStateFlow(false)

  val playing: StateFlow<Boolean> = _playing
  val rhythm: StateFlow<RhythmData> = _rhythm

  suspend fun load() {
    _rhythm.value = Updated(rhythmStore.load())
  }

  suspend fun save() {
    (_rhythm.value as? Updated)?.let {
      rhythmStore.save(it.rhythmModel)
    }
  }

  fun setPlaying(playing: Boolean) {
    _playing.value = playing
  }

  fun setBpm(bpm: Int) {
    (_rhythm.value as? Updated)?.let {
      _rhythm.value = Updated(
        it.rhythmModel.copy(bpm = bpm)
      )
    }
  }

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
