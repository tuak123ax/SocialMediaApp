package com.minhtu.firesocialmedia.domain.serviceimpl.call

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.platform.logMessage

object CallSoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isRingtonePlayed = false

    fun playRingtone(context: Context) {
        if (isRingtonePlayed) return

        stopRingtone()
        try {
            val afd = context.resources.openRawResourceFd(R.raw.call_ringtone)
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_RING)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                prepare()
                start()
            }
            isRingtonePlayed = true
        } catch (e: Exception) {
            e.printStackTrace()
            logMessage("Ringtone") { "Failed to play custom ringtone: ${e.message}" }
        }
    }

    fun stopRingtone() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isRingtonePlayed = false
    }
}
