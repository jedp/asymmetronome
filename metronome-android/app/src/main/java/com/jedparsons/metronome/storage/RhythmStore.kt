package com.jedparsons.metronome.storage

import com.jedparsons.metronome.model.RhythmModel

/**
 * Storage API for rhythm data.
 */
interface RhythmStore {

  suspend fun save(rhythmModel: RhythmModel)

  suspend fun load(): RhythmModel
}
