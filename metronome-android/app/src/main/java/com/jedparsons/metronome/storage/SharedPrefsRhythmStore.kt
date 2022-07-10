package com.jedparsons.metronome.storage

import android.content.SharedPreferences
import com.jedparsons.metronome.model.RhythmModel
import com.jedparsons.metronome.model.RhythmModel.Companion.DEFAULT_BPM
import com.jedparsons.metronome.model.RhythmModel.Companion.DEFAULT_DIVISIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * [RhythmStore] backed by shared prefs.
 */
class SharedPrefsRhythmStore(
  private val sharedPrefs: SharedPreferences,
  private val context: CoroutineContext = Dispatchers.IO
) : RhythmStore {

  companion object {
    const val BPM_KEY = "bpm"
    const val DIV_KEY = "div"
  }

  override suspend fun save(rhythmModel: RhythmModel) {
    withContext(context) {
      check(rhythmModel.divisions.isNotEmpty()) { "Divisions cannot be empty" }
      check(rhythmModel.bpm > 0) { "BPM cannot be negative" }

      with(sharedPrefs.edit()) {
        putInt(BPM_KEY, rhythmModel.bpm)
        putString(DIV_KEY, rhythmModel.divisions.joinToString(","))
        commit()
      }
    }
  }

  override suspend fun load(): RhythmModel {
    return withContext(context) {
      RhythmModel(
        bpm = sharedPrefs.getInt(
          BPM_KEY, DEFAULT_BPM
        ),
        divisions = deserializeDivisions(
          sharedPrefs.getString(
            DIV_KEY, serializeDivisions(DEFAULT_DIVISIONS)
          )
        )
      )
    }
  }

  private fun serializeDivisions(divisions: List<Int>): String {
    return divisions.joinToString(",")
  }

  private fun deserializeDivisions(serialized: String?): List<Int> {
    return serialized
      ?.split(",")
      ?.map { s -> s.toInt() }
      ?.toList()
      ?: DEFAULT_DIVISIONS
  }
}
