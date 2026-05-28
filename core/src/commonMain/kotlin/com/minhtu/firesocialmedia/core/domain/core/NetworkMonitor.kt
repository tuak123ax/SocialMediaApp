package com.minhtu.firesocialmedia.core.domain.core

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}