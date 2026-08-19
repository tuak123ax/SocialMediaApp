package com.minhtu.firesocialmedia.data.remote.service.imagepicker.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.minhtu.firesocialmedia.ios.service.serviceimpl.imagepicker.home.IosImagePicker

@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit,
    onVideoPicked: (String) -> Unit
): ImagePicker {
    return remember { IosImagePicker(onImagePicked, onVideoPicked).imagePicker }
}
