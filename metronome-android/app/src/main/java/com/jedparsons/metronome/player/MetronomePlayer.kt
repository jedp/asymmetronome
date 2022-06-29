package com.jedparsons.metronome.player

import android.content.res.AssetManager
import android.util.Log
import java.io.IOException

/**
 * Interface to native metronome player.
 */
class MetronomePlayer {

  fun setupAudioStream() = setupAudioStreamNative(NUM_PLAY_CHANNELS)

  fun startAudioStream() = startAudioStreamNative()

  fun teardownAudioStream() = teardownAudioStreamNative()

  fun loadWavAssets(assetMgr: AssetManager): Boolean {
    var returnVal = false
    try {
      val assetFD = assetMgr.openFd(WAV_ASSET)
      val dataStream = assetFD.createInputStream()
      val dataLen = assetFD.length.toInt()
      val dataBytes = ByteArray(dataLen)
      dataStream.read(dataBytes, 0, dataLen)
      returnVal = loadWavAssetNative(dataBytes, NUM_SAMPLE_CHANNELS)
      assetFD.close()
    } catch (ex: IOException) {
      Log.i(TAG, "IOException: $ex")
    }

    Log.i(TAG, "Loaded $WAV_ASSET")
    return returnVal
  }

  fun unloadWavAssets() = unloadWavAssetsNative()

  fun triggerDownBeat() = trigger()

  fun setGain(gain: Float) = setAudioGain(gain)

  private external fun setupAudioStreamNative(numChannels: Int)
  private external fun startAudioStreamNative()
  private external fun teardownAudioStreamNative()
  private external fun loadWavAssetNative(wavBytes: ByteArray, channels: Int): Boolean
  private external fun unloadWavAssetsNative()
  private external fun trigger()
  private external fun setAudioGain(gain: Float)
  private external fun getOutputReset(): Boolean
  private external fun clearOutputReset()
  private external fun restartStream()

  companion object {
    const val TAG = "MetronomePlayer"
    const val WAV_ASSET = "HandDrum.wav"

    // Sample attributes
    const val NUM_PLAY_CHANNELS: Int = 2  // The number of channels in the player Stream.

    // Stereo Playback, set to 1 for Mono playback
    // This IS NOT the channel format of the source samples
    // (which must be mono).
    const val NUM_SAMPLE_CHANNELS: Int = 1   // All WAV resource must be mon
  }
}