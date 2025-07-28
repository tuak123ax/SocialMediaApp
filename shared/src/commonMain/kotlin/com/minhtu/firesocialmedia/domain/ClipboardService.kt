package com.minhtu.firesocialmedia.domain

import io.mockative.Mockable

@Mockable
interface ClipboardService {
    fun copy(text: String)
}