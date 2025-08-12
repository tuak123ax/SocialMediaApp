package com.minhtu.firesocialmedia.domain.serviceimpl.clipboard

import io.mockative.Mockable

@Mockable
interface ClipboardService {
    fun copy(text: String)
}