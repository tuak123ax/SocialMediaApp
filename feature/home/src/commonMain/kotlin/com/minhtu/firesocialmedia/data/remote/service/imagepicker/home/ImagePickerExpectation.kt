package com.minhtu.firesocialmedia.data.remote.service.imagepicker.home

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit,
    onVideoPicked: (String) -> Unit
): ImagePicker
