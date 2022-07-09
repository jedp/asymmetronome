package com.jedparsons.metronome

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.jedparsons.metronome.model.RhythmModel
import com.jedparsons.metronome.storage.SharedPrefsRhythmStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class SharedPrefsRhythmStoreTest {

  private val editor = mock<SharedPreferences.Editor>()
  private val prefs = mock<SharedPreferences> {
    on { edit() } doReturn (editor)
    on { getInt(eq("bpm"), any()) } doReturn (350)
    on { getString(eq("div"), any()) } doReturn ("4,2,3")
  }
  private val activity = mock<Activity> {
    on { getPreferences(MODE_PRIVATE) } doReturn (prefs)
  }

  private val store = SharedPrefsRhythmStore(activity)

  @Test
  fun `save serializes correctly`() = runTest {
    launch {
      store.save(
        RhythmModel(
          bpm = 123,
          divisions = listOf(3, 2, 2)
        )
      )
    }

    advanceUntilIdle()

    verify(editor).putInt(eq("bpm"), eq(123))
    verify(editor).putString(eq("div"), eq("3,2,2"))
    verify(editor).commit()
  }

  @Test
  fun `load deserializes correctly`() = runTest {
    launch {
      val rhythm = store.load()
      assertThat(rhythm.bpm).isEqualTo(350)
      assertThat(rhythm.divisions).isEqualTo(listOf(4, 2, 3))
    }

    advanceUntilIdle()

    verify(prefs).getInt(eq("bpm"), any())
    verify(prefs).getString(eq("div"), any())
  }
}
