package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.platform.logMessage

object CallSoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isRingtonePlayed = false

    /** True while the callee ringtone is actively playing (phone is ringing for an incoming call). */
    val isRinging: Boolean get() = isRingtonePlayed

    // ── Callee ringtone (incoming call) ────────────────────────────────────────
    fun playRingtone(context: Context) {
        if (isRingtonePlayed) return
        releasePlayer()
        try {
            val afd = context.resources.openRawResourceFd(R.raw.call_ringtone)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_RING)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                start()
            }
            isRingtonePlayed = true
        } catch (e: Exception) {
            e.printStackTrace()
            logMessage("Ringtone") { "Failed to play callee ringtone: ${e.message}" }
        }
    }

    fun stopRingtone() {
        releasePlayer()
    }

    // ── Caller ringtone (outgoing call waiting) ────────────────────────────────
    // Do NOT set AudioManager.mode = MODE_RINGTONE here — it causes the OS to play
    // the default system ringtone in parallel with our MediaPlayer.
    //
    // AndroidCallService.setupAudioManager() sets MODE_IN_COMMUNICATION before this
    // is called, which silences STREAM_RING / USAGE_NOTIFICATION_RINGTONE entirely.
    // We must use USAGE_VOICE_COMMUNICATION + STREAM_VOICE_CALL, which ARE audible
    // in MODE_IN_COMMUNICATION (same stream WebRTC uses for audio playback).
    fun playRingtoneForCaller(context: Context) {
        if (isRingtonePlayed) return
        releasePlayer()
        try {
            val afd = context.resources.openRawResourceFd(R.raw.calling_ringtone)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                setOnPreparedListener { start() }
                prepareAsync()
            }
            isRingtonePlayed = true
        } catch (e: Exception) {
            e.printStackTrace()
            logMessage("Ringtone") { "Failed to play caller ringtone: ${e.message}" }
        }
    }

    fun stopRingtoneForCaller() {
        releasePlayer()
    }

    // ── Shared release helper ──────────────────────────────────────────────────
    private fun releasePlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (_: Exception) { }
        mediaPlayer = null
        isRingtonePlayed = false
    }
}
