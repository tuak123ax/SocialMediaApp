package com.minhtu.firesocialmedia.data.remote.service.imagepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ImagePicker {
    @Composable
    fun RegisterLauncher(hideLoading : () -> Unit)
    fun pickImage()
    fun pickVideo()
    suspend fun loadImageBytes(uri: String): ByteArray?
    @Composable
    fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier)
    fun captureImage()
    fun captureVideo()
}