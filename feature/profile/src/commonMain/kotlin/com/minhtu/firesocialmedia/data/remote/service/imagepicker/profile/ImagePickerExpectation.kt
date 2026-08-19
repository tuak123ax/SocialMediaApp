package com.minhtu.firesocialmedia.data.remote.service.imagepicker.profile

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit
): ImagePicker
