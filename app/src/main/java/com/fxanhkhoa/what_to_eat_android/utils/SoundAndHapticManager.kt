package com.fxanhkhoa.what_to_eat_android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@SuppressLint("MissingPermission")
class SoundAndHapticManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var spinSoundId: Int = -1
    private var spinStreamId: Int = -1
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the spinning sound if the resource exists
        try {
            val resId = context.resources.getIdentifier("wheel_spin", "raw", context.packageName)
            if (resId != 0) {
                spinSoundId = soundPool?.load(context, resId, 1) ?: -1
            }
        } catch (e: Exception) {
            // Sound resource not found, continue without sound
        }
    }

    /**
     * Play the spinning sound
     */
    fun playSpinSound() {
        if (spinSoundId != -1) {
            spinStreamId = soundPool?.play(spinSoundId, 0.7f, 0.7f, 1, 0, 1.0f) ?: -1
        }
    }

    /**
     * Stop the spinning sound
     */
    fun stopSpinSound() {
        if (spinStreamId != -1) {
            soundPool?.stop(spinStreamId)
            spinStreamId = -1
        }
    }

    /**
     * Perform haptic feedback for spinning
     */
    fun performSpinHaptic() {
        vibrator?.let {
            // Create a pattern for spinning: short pulses
            val timings = longArrayOf(0, 50, 50, 50, 50, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 100, 0, 100, 0, 100)
            val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            it.vibrate(vibrationEffect)
        }
    }

    /**
     * Perform haptic feedback for wheel landing
     */
    fun performLandingHaptic() {
        vibrator?.let {
            val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            it.vibrate(vibrationEffect)
        }
    }

    /**
     * Perform light haptic feedback (for button taps, etc.)
     */
    fun performLightHaptic() {
        vibrator?.let {
            val vibrationEffect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            it.vibrate(vibrationEffect)
        }
    }

    fun release() {
        stopSpinSound()
        soundPool?.release()
        soundPool = null
    }
}

@Composable
fun rememberSoundAndHapticManager(): SoundAndHapticManager {
    val context = LocalContext.current
    val manager = remember { SoundAndHapticManager(context) }

    DisposableEffect(manager) {
        onDispose {
            manager.release()
        }
    }

    return manager
}


