package com.minhtu.firesocialmedia.core.domain.entity.home.deeplinks

import com.seiko.imageloader.Bitmap

data class ShareApp(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Bitmap?
)