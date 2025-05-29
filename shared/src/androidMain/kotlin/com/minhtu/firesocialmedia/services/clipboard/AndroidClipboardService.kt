package com.minhtu.firesocialmedia.services.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.minhtu.firesocialmedia.ClipboardService

class AndroidClipboardService(
    private val context : Context) : ClipboardService {
    override fun copy(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied text", text)
        clipboard.setPrimaryClip(clip)
    }
}