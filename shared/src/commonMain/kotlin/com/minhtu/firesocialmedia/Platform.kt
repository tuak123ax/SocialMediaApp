package com.minhtu.firesocialmedia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
