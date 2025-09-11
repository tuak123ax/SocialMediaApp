package com.minhtu.firesocialmedia.domain.service.clipboard

import io.mockative.Mockable

@Mockable
interface ClipboardService {
    fun copy(text: String)
}