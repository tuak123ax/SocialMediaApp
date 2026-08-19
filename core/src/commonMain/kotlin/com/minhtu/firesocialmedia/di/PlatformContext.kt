package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.network.NetworkMonitor
import io.mockative.Mockable

@Mockable
interface PlatformContext {
    val networkMonitor : NetworkMonitor
}
