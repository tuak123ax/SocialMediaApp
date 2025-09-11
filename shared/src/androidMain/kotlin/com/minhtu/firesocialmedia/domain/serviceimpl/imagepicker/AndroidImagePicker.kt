package com.minhtu.firesocialmedia.domain.serviceimpl.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import com.minhtu.firesocialmedia.domain.service.imagepicker.ImagePicker
import java.io.ByteArrayOutputStream

class AndroidImagePicker(
    private val context : Context,
    private val onImagePicked: (String) -> Unit,
    private val onVideoPicked: (String) -> Unit
) : ImagePicker {
    enum class PickType { IMAGE, VIDEO }
    var currentPickType: PickType? = null
    private lateinit var launcher: ActivityResultLauncher<Intent>
    @Composable
    override fun RegisterLauncher(hideLoading : () -> Unit) {
        launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                Log.e("getAvatarFromGalleryLauncher", "RESULT_OK")
                val dataUrl = result.data?.data
                if(dataUrl != null){
                    Log.e("getAvatarFromGalleryLauncher", dataUrl.toString())
                    if(currentPickType == PickType.IMAGE){
                        onImagePicked(dataUrl.toString())
                    } else {
                        onVideoPicked(dataUrl.toString())
                    }
                    hideLoading()
                }
            } else {
                if(result.resultCode == Activity.RESULT_CANCELED)
                {
                    currentPickType = null
                    hideLoading()
                }
            }
        }
    }

    override fun pickImage() {
        currentPickType = PickType.IMAGE
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcher.launch(intent)
    }

    override fun pickVideo() {
        currentPickType = PickType.VIDEO
        val intent = Intent()
        intent.setType("video/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcher.launch(intent)
    }

    override suspend fun loadImageBytes(uri: String): ByteArray? {
        return try {
            val parsedUri = uri.toUri()
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, parsedUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, parsedUri)
            }

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    override fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier) {
        if(byteArray != null) {
            val bitmap = remember(byteArray) { byteArray.toBitmap() }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            }
        }
    }

    fun ByteArray.toBitmap(): Bitmap? {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }
}