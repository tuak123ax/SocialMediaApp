package com.minhtu.firesocialmedia.domain.serviceimpl.clipboard

import com.minhtu.firesocialmedia.domain.service.clipboard.ClipboardService
import platform.UIKit.UIPasteboard

class IosClipboardService : ClipboardService {
    override fun copy(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}