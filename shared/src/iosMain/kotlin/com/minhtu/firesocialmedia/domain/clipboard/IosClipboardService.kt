package com.minhtu.firesocialmedia.domain.clipboard

import com.minhtu.firesocialmedia.di.ClipboardService
import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {
    override fun copy(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}