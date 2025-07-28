package com.minhtu.firesocialmedia.domain.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.minhtu.firesocialmedia.domain.ClipboardService

class AndroidClipboardService(
    private val context : Context) : ClipboardService {
    override fun copy(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied text", text)
        clipboard.setPrimaryClip(clip)
    }
}