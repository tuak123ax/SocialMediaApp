package com.minhtu.firesocialmedia.ios.service.serviceimpl.imagepicker.auth

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.auth.ImagePicker
import com.minhtu.firesocialmedia.ios.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.base64ToByteArray
import com.minhtu.firesocialmedia.platform.toBase64String
import io.ktor.client.request.request
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpMethod
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.getBytes
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaType
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

class IosImagePicker(
    private val onImagePicked: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    val imagePicker = object : ImagePicker {
        override fun pickImage() {
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                delegate = this@IosImagePicker
            }
            val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootController?.presentViewController(picker, animated = true, completion = null)
        }

        override suspend fun loadImageBytes(byteString: String): ByteArray? {
            if(byteString.contains("https://")){
                return downloadImageAsByteArray(byteString)
            } else {
                return byteString.base64ToByteArray()
            }
        }

        suspend fun downloadImageAsByteArray(url: String): ByteArray {
            val response = KtorProvider.client.request(url) {
                method = HttpMethod.Get
            }
            return response.readBytes()
        }

        @Composable
        override fun RegisterLauncher(hideLoading: () -> Unit) {
            // No launcher registration needed in iOS
        }

        @Composable
        override fun ByteArrayImage(byteArray: ByteArray?, modifier: Modifier) {
            byteArray?.let { bytes ->
                val imageBitmap = bytes.toImageBitmap()
                imageBitmap?.let { bitmap ->
                    Image(
                        painter = BitmapPainter(bitmap),
                        contentDescription = "Picked image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                    )
                }
            }
        }
    }

    private val mainScope = MainScope()


    // Called when user picks an image
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val mediaType = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaType] as? String
        if (mediaType == "public.image") {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            // Handle picked image
            if (image != null) {
                mainScope.launch {
                    val byteArray = withContext(Dispatchers.Default) { image.toByteArray() }
                    if(byteArray != null) {
                        onImagePicked(byteArray.toBase64String())
                    }
                }
            }
        }
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    // Convert UIImage to ByteArray (PNG representation)
    fun UIImage.toByteArray(): ByteArray? {
        val imageData = UIImagePNGRepresentation(this) ?: return null
        return imageData.toByteArray()
    }

    fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            this.getBytes(pinned.addressOf(0))
        }
        return bytes
    }

    // Convert ByteArray to Compose ImageBitmap
    private fun ByteArray.toImageBitmap(): ImageBitmap? {
        return try {
            Image.makeFromEncoded(this).toComposeImageBitmap()
        } catch (e: Exception) {
            println("Image decode failed: ${e.message}")
            null
        }
    }
}
