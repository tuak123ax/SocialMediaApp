package com.minhtu.firesocialmedia.data.remote.service.imagepicker.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ImagePicker {
    @Composable
    fun RegisterLauncher(hideLoading : () -> Unit)
    fun pickImage()
    suspend fun loadImageBytes(uri: String): ByteArray?
    @Composable
    fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier)
}
