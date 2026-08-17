package com.minhtu.firesocialmedia.data.remote.service.imagepicker.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.minhtu.firesocialmedia.ios.service.serviceimpl.imagepicker.group.IosImagePicker

@Composable
actual fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit
): ImagePicker {
    return remember { IosImagePicker(onImagePicked).imagePicker }
}
