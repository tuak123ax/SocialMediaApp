package com.minhtu.firesocialmedia.data.remote.service.imagepicker.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.minhtu.firesocialmedia.data.remote.auth.service.imagepicker.auth.ImagePicker
import com.minhtu.firesocialmedia.ios.service.serviceimpl.imagepicker.auth.IosImagePicker

@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit
): ImagePicker {
    return remember { IosImagePicker(onImagePicked).imagePicker }
}
