package com.minhtu.firesocialmedia.domain.call

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.platform.logMessage

object CallSoundManager {
    private var mediaPlayer: MediaPlayer? = null

    fun playRingtone(context: Context) {
        stopRingtone() // stop any previous sound

        try {
            val afd = context.resources.openRawResourceFd(R.raw.call_ringtone)
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_RING)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logMessage("Ringtone", { "Failed to play custom ringtone: ${e.message}" })
        }
    }

    fun stopRingtone() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }
}