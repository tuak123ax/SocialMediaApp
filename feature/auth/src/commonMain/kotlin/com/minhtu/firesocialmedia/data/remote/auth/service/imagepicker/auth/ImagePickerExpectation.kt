package com.minhtu.firesocialmedia.data.remote.auth.service.imagepicker.auth

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformImagePicker(
    context: Any?,
    onImagePicked: (String) -> Unit
): ImagePicker
