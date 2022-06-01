package com.jedparsons.metronome

import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
import com.jedparsons.metronome.BeatsPerMinuteViewModel.Companion.DEFAULT_BPM
import com.jedparsons.metronome.player.MetronomePlayer
import com.jedparsons.metronome.ui.theme.Amber
import com.jedparsons.metronome.ui.theme.DarkAmber
import com.jedparsons.metronome.ui.theme.MetronomeTheme
import java.util.Timer
import java.util.TimerTask

class MetronomeActivity : ComponentActivity() {

  private val beatsPerMinuteViewModel: BeatsPerMinuteViewModel by viewModels()
  private val playButtonViewModel: PlayButtonViewModel by viewModels()
  private val subdivisionsViewModel: SubdivisionsViewModel by viewModels()

  private var metronomePlayer = MetronomePlayer()

  init {
    System.loadLibrary("metronome")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val metronomeController = MetronomeController(metronomePlayer)

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

    // Let the metronome controller directly observe the viewmodels' states.
    // The audio player is like another output device coupled to the UI.
    beatsPerMinuteViewModel.bpm.observe(this) {
      metronomeController.bpm = it
    }
    playButtonViewModel.playing.observe(this) { play ->
      if (play) metronomeController.start() else metronomeController.stop()
    }
    subdivisionsViewModel.values.observe(this) {
      metronomeController.subdivisions = it
    }

    window.addFlags(FLAG_KEEP_SCREEN_ON)
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

  override fun onStop() {
    metronomePlayer.teardownAudioStream()
    metronomePlayer.unloadWavAssets()
    Log.i(TAG, "Cleaned up audio stream")

    super.onStop()
  }

  private fun saveState() {
    // Stash bpm and divisions in shared prefs. Easy peasy.
    val sharedPref = getPreferences(MODE_PRIVATE)
    with(sharedPref.edit()) {
      putInt(BPM_KEY, beatsPerMinuteViewModel.bpm.value ?: DEFAULT_BPM)
      // Serialize the list of ints to a string like "1,2,3".
      putString(DIV_KEY, subdivisionsViewModel.values.value?.joinToString(",") ?: "1,0")
      commit()
    }
  }

  private fun restoreState() {
    // Restore bpm and divisions from shared prefs.
    val sharedPref = getPreferences(MODE_PRIVATE)
    beatsPerMinuteViewModel.updateBPM(sharedPref.getInt(BPM_KEY, DEFAULT_BPM))
    // Convert a string like "1,2,3" back to a list of ints.
    subdivisionsViewModel.updateValues(
      sharedPref.getString(DIV_KEY, "1,0")!!
        .split(",")
        .map { s -> s.toInt() }
        .toList()
    )
  }

  companion object {
    const val TAG = "MetronomeActivity"
    const val BPM_KEY = "bpm"
    const val DIV_KEY = "div"
  }
}

/**
 * Drives native metronome according to bpm and subdivisions of the rhythm.
 */
class MetronomeController(
  private val player: MetronomePlayer
) {

  private var timer: Timer? = null
  private var lastBeat: Long = 0
  private var currentEmphasis = 0
  private var emphasis: List<Int> = listOf(1)

  var bpm: Int = 120
  var subdivisions: List<Int> = listOf(1)
    set(value) {
      // Map the subdivisions from the view model to a list of 1s and 0s, one per each beat.
      // E.g. (4, 2, 3) -> (1, 0, 0, 0, 1, 0, 1, 0, 0)
      // We'll just loop through this processed list when setting the gain for each beat.
      field = value
      emphasis = field
        .filter { it > 0 }
        .map { v ->
          mutableListOf(1).apply {
            repeat(v - 1) { this += 0 }
          }
        }.flatten()
    }

  fun start() {
    stop()
    val t = Timer()
    timer = t
    // Loop forever. On each iteration, see if it's time to play a beat, and with what emphasis.
    t.schedule(object : TimerTask() {
      override fun run() {
        val now = System.currentTimeMillis()
        if (now >= (60000 / bpm) + lastBeat) {
          player.triggerDownBeat()
          lastBeat = now
          if (currentEmphasis >= emphasis.size) {
            currentEmphasis = 0
          }
          player.setGain(if (emphasis[currentEmphasis] == 1) MAX_GAIN else LOW_GAIN)
          currentEmphasis += 1
        }
      }
    }, 0, 30)
  }

  fun stop() {
    timer?.cancel()
    timer = null
  }

  companion object {
    const val MAX_GAIN = 1.8f
    const val LOW_GAIN = 0.4f
  }
}

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

  fun onDrag(offset: Offset) {
    val increment = if (offset.x < 0) -1 else 1
    updateBPM((_bpm.value ?: DEFAULT_BPM) + increment)
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
class SubdivisionsViewModel : ViewModel() {

  private val _values: MutableLiveData<List<Int>> = MutableLiveData(listOf(1, 0))

  val values: LiveData<List<Int>> = _values

  fun onDrag(
    item: Int,
    offset: Offset
  ) {
    if (offset.x < -5) {
      updateValue(item, _values.value?.let { it[item] - 1 } ?: MIN)
    } else if (offset.x > 5) {
      updateValue(item, _values.value?.let { it[item] + 1 } ?: MIN)
    }
  }

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

    if (item == temp.size - 1 && temp[item] != 0) {
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

  Column(
    Modifier.fillMaxHeight(),
    verticalArrangement = Arrangement.SpaceEvenly,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    BeatsPerMinuteContent(
      beatsPerMinute = bpm,
      onDrag = beatsPerMinuteViewModel::onDrag
    )
    TappableButton(
      text = stringResource(R.string.tap).toUpperCase(Locale.current),
      onTap = beatsPerMinuteViewModel::onTap
    )
    SubdivisionsContent(
      divisions = subdivisions,
      onDrag = subdivisionsViewModel::onDrag
    )
    TappableButton(
      text = stringResource(if (playing) R.string.stop else R.string.start)
        .toUpperCase(Locale.current),
      onTap = playButtonViewModel::onClick
    )
  }
}

/**
 * Display of the number of beats per minute.
 */
@Composable
fun BeatsPerMinuteContent(
  beatsPerMinute: Int,
  onDrag: (offset: Offset) -> Unit
) {
  Box(
    Modifier
      .width(330.dp)
      .pointerInput(Unit) {
        detectDragGestures { _, offset ->
          onDrag(offset)
        }
      },
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = beatsPerMinute.toString(),
      fontSize = 160.sp,
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
  onDrag: (item: Int, offset: Offset) -> Unit
) {
  LazyRow(
    verticalAlignment = Alignment.CenterVertically
  ) {
    itemsIndexed(divisions) { i, beats ->
      SubdivisionContent(
        beats = beats,
        isLast = i == divisions.size - 1,
        onDrag = { offset ->
          onDrag(i, offset)
        }
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
  onDrag: (offset: Offset) -> Unit
) {
  Box(
    Modifier
      .pointerInput(Unit) {
        detectDragGestures { _, offset ->
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
