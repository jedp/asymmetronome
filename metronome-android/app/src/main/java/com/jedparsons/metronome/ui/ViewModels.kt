package com.jedparsons.metronome.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jedparsons.metronome.R
import com.jedparsons.metronome.ui.theme.Amber
import com.jedparsons.metronome.ui.theme.DarkAmber

/**
 * Beats per minute.
 *
 * State: bpm
 *
 * Can be set via onDrag() handler and onTap() handler.
 */
class BeatsPerMinuteViewModel : ViewModel() {

  private var lastTap = System.currentTimeMillis()

  private val _bpm: MutableLiveData<Int> = MutableLiveData(DEFAULT_BPM)

  val bpm: LiveData<Int> = _bpm

  fun incrementValueBy(newValue: Int) {
    updateBPM((_bpm.value ?: DEFAULT_BPM) + newValue)
  }

  fun onTap() {
    val now = System.currentTimeMillis()
    val bpm = (60000 / (now - lastTap)).toInt()
    updateBPM(bpm)
    lastTap = now
  }

  fun updateBPM(value: Int) {
    _bpm.value = value.coerceIn(MIN_BPM, MAX_BPM)
  }

  companion object {
    const val DEFAULT_BPM = 120
    const val MIN_BPM = 1
    const val MAX_BPM = 500
  }
}

/**
 * Subdivisions state: values = list of beats per subdivision.
 *
 * Enforces: First element can never be less than 1. Last element must always be 0.
 */
class SubdivisionsViewModel(
  private val maxSubdivisions: Int = 4
) : ViewModel() {

  private val _values: MutableLiveData<List<Int>> = MutableLiveData(listOf(1, 0))

  val values: LiveData<List<Int>> = _values

  fun incrementItemValueBy(
    item: Int,
    newValue: Int
  ) = updateValue(item, _values.value?.let { it[item] + newValue } ?: 0)

  fun updateValues(values: List<Int>) {
    _values.value = values
  }

  private fun updateValue(
    item: Int,
    value: Int
  ) {
    _values.value?.let { if (item >= it.size) return }

    var temp = mutableListOf<Int>()
    temp.addAll(_values.value?.toList() ?: emptyList())
    if (item == 0) {
      // First subdivision always has at least one beat.
      temp[item] = value.coerceIn(1, MAX)
    } else {
      temp[item] = value.coerceIn(MIN, MAX)
      if (temp[item] == 0) {
        temp = temp.subList(0, item + 1)
      }
    }

    if (temp.size < maxSubdivisions && item == temp.size - 1 && temp[item] != 0) {
      temp.add(0)
    }
    updateValues(temp)
  }

  companion object {
    const val MIN = 0
    const val MAX = 16
  }
}

/**
 * Play button states: playing = true or false.
 */
class PlayButtonViewModel : ViewModel() {

  private var _playing: MutableLiveData<Boolean> = MutableLiveData(false)

  val playing: LiveData<Boolean> = _playing

  fun onClick() {
    _playing.value = _playing.value?.let { !it } ?: false
  }

  fun stop() {
    _playing.value = false
  }
}

/**
 * The complete metronome screen.
 *
 * At the top, we see beats per minute. Dragging left or right changes the tempo.
 *
 * The TAP button can be used to set the tempo by tapping.
 *
 * The beat subdivisions allow you to set the subdivisions of the meter by dragging left or right
 * on the values. There will always be a "0" at the end of the list, offering a way to add a new
 * value. Dragging a value to 0 causes the following values to be truncated. On playback, the
 * first beat of each subdivision will be stressed.
 *
 * The START / STOP button starts or stops the metronome.
 */
@Composable
fun MetronomeScreen(
  beatsPerMinuteViewModel: BeatsPerMinuteViewModel,
  playButtonViewModel: PlayButtonViewModel,
  subdivisionsViewModel: SubdivisionsViewModel
) {

  val bpm: Int by beatsPerMinuteViewModel.bpm.observeAsState(initial = 120)
  val playing: Boolean by playButtonViewModel.playing.observeAsState(initial = false)
  val subdivisions: List<Int> by subdivisionsViewModel.values.observeAsState(initial = listOf(1, 0))

  BoxWithConstraints(
    contentAlignment = Alignment.Center
  ) {
    if (maxWidth < 400.dp) {
      // Portrait: Single column.
      Column(
        Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        BeatsPerMinuteContent(
          beatsPerMinute = bpm,
          incrementValueBy = beatsPerMinuteViewModel::incrementValueBy
        )
        TappableButton(
          text = stringResource(R.string.tap).toUpperCase(Locale.current),
          onTap = beatsPerMinuteViewModel::onTap
        )
        SubdivisionsContent(
          divisions = subdivisions,
          incrementItemValueBy = subdivisionsViewModel::incrementItemValueBy
        )
        TappableButton(
          text = stringResource(if (playing) R.string.stop else R.string.start)
            .toUpperCase(Locale.current),
          onTap = playButtonViewModel::onClick
        )
      }
    } else {
      // Landscape: Two columns.
      Row(
        Modifier.fillMaxSize(),
        Arrangement.SpaceEvenly
      ) {
        Box(
          Modifier
            .fillMaxHeight()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            Modifier
              .fillMaxHeight()
              .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            BeatsPerMinuteContent(
              beatsPerMinute = bpm,
              incrementValueBy = beatsPerMinuteViewModel::incrementValueBy
            )
            TappableButton(
              text = stringResource(R.string.tap).toUpperCase(Locale.current),
              onTap = beatsPerMinuteViewModel::onTap
            )
          }
        }
        Box(
          Modifier
            .fillMaxHeight()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            Modifier
              .fillMaxHeight()
              .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Spacer(
              Modifier.padding(20.dp)
            )
            SubdivisionsContent(
              divisions = subdivisions,
              incrementItemValueBy = subdivisionsViewModel::incrementItemValueBy
            )
            TappableButton(
              text = stringResource(if (playing) R.string.stop else R.string.start)
                .toUpperCase(Locale.current),
              onTap = playButtonViewModel::onClick
            )
          }
        }
      }
    }
  }
}

/**
 * Display of the number of beats per minute.
 */
@Composable
fun BeatsPerMinuteContent(
  beatsPerMinute: Int,
  incrementValueBy: (newValue: Int) -> Unit
) {
  val horizontalDragHandler = HorizontalDragHandler(
    dpPerUnit = 10,
    incrementValueBy = incrementValueBy
  )
  Box(
    Modifier
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = horizontalDragHandler::onDragStart
        ) { _, offset ->
          horizontalDragHandler.onDrag(offset)
        }
      }
  ) {
    Text(
      text = beatsPerMinute.toString(),
      fontSize = 150.sp,
      style = MaterialTheme.typography.h1,
    )
  }
}

/**
 * A tappable button with a border.
 */
@Composable
fun TappableButton(
  text: String,
  onTap: () -> Unit
) {
  Box(
    Modifier.pointerInput(Unit) {
      forEachGesture {
        awaitPointerEventScope {
          awaitFirstDown()
          onTap()
        }
      }
    },
    contentAlignment = Alignment.Center
  ) {
    Card(
      border = BorderStroke(1.dp, color = Amber),
      shape = RoundedCornerShape(10)
    ) {
      Text(
        text = text,
        modifier = Modifier.padding(8.dp),
        fontSize = 60.sp,
        style = MaterialTheme.typography.body1
      )
    }
  }
}

/**
 * Displays all the subdivisions of the rhythm in a row.
 *
 * E.g., 4 + 2 + 3 + 0
 *
 * The last element is always 0.
 *
 * If an element in the middle of the row is reduced to 0, the following elements are truncated.
 *
 * E.g., 4 + 0 + 3 + 0 -> 4 + 0
 */
@Composable
fun SubdivisionsContent(
  divisions: List<Int>,
  incrementItemValueBy: (item: Int, newValue: Int) -> Unit
) {
  LazyRow(
    verticalAlignment = Alignment.CenterVertically
  ) {
    itemsIndexed(divisions) { i, beats ->
      val dragHandler = HorizontalDragHandler(incrementValueBy = {
        incrementItemValueBy(i, it)
      })

      SubdivisionContent(
        beats = beats,
        isLast = i == divisions.size - 1,
        onDragStart = dragHandler::onDragStart,
        onDrag = dragHandler::onDrag
      )
    }
  }
}

/**
 * Displays one of the subdivisions of the rhythm.
 *
 * Drag left or right to increase or decrease value.
 */
@Composable
fun SubdivisionContent(
  beats: Int,
  isLast: Boolean,
  onDragStart: (Offset) -> Unit,
  onDrag: (Offset) -> Unit
) {
  Box(
    Modifier
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset -> onDragStart(offset) }
        ) { _, offset ->
          onDrag(offset)
        }
      }
  ) {
    Text(
      text = beats.toString(),
      fontSize = 60.sp,
      color = if (beats > 0) Amber else DarkAmber,
      style = MaterialTheme.typography.body1
    )
  }
  if (!isLast) {
    Text(
      text = "+",
      fontSize = 40.sp,
      color = DarkAmber,
      style = MaterialTheme.typography.body1
    )
  }
}
