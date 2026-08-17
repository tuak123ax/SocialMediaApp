package com.minhtu.firesocialmedia.group.entity.deeplinks

import com.seiko.imageloader.Bitmap

data class ShareApp(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Bitmap?
)
