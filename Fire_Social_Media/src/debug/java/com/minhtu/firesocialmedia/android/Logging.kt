package com.minhtu.firesocialmedia.android

import timber.log.Timber

internal fun setupTimberLogging() {
    Timber.plant(Timber.DebugTree())
}
