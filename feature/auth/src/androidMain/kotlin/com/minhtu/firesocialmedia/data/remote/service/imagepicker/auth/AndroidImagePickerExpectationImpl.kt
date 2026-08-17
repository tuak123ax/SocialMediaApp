package com.minhtu.firesocialmedia.data.remote.service.imagepicker.auth

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.minhtu.firesocialmedia.android.service.serviceimpl.imagepicker.auth.AndroidImagePicker

@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit
): ImagePicker {
    val ctx = when (context) {
        is Activity -> context
        is Context -> context
        else -> LocalContext.current
    }
    return remember(ctx) { AndroidImagePicker(ctx, onImagePicked) }
}
