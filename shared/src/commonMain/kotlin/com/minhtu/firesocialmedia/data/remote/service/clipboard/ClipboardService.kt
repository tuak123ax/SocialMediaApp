package com.minhtu.firesocialmedia.data.remote.service.clipboard

import io.mockative.Mockable

@Mockable
interface ClipboardService {
    fun copy(text: String)
}