package com.minhtu.firesocialmedia.ios.service.serviceimpl.clipboard

import com.minhtu.firesocialmedia.data.remote.service.clipboard.ClipboardService
import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {
    override fun copy(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}