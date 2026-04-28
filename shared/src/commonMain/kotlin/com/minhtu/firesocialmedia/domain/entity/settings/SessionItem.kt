package com.minhtu.firesocialmedia.domain.entity.settings

data class SessionItem(
    val deviceName: String = "",
    val location: String = "",
    val time: Long = 0L,
    val current: Boolean = false,
    val activeNow: Boolean = false
)