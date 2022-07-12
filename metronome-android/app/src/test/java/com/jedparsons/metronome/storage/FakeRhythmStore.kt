package com.jedparsons.metronome.storage

import com.jedparsons.metronome.model.RhythmModel
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class FakeRhythmStore(
  private val context: CoroutineContext
) : RhythmStore {

  private var rhythmModel: RhythmModel = RhythmModel()

  fun setNextData(rhythmModel: RhythmModel) {
    this.rhythmModel = rhythmModel
  }

  override suspend fun save(rhythmModel: RhythmModel) {
    withContext(context) {
      this@FakeRhythmStore.rhythmModel = rhythmModel
    }
  }

  override suspend fun load(): RhythmModel {
    return withContext(context) {
      rhythmModel.copy()
    }
  }
}
