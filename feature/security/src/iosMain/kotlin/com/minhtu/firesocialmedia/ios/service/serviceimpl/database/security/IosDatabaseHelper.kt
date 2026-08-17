package com.minhtu.firesocialmedia.ios.service.serviceimpl.database.security

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataTaskWithURL
import platform.Foundation.writeToURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import kotlin.coroutines.resume

class IosDatabaseHelper {
    companion object {
        suspend fun downloadImage(imageUrl: String, fileName: String) : Boolean = suspendCancellableCoroutine { continuation ->
            val url = NSURL(string = imageUrl)

            val session = NSURLSession.sharedSession

            val task = session.dataTaskWithURL(url) { data, response, error ->
                if (error != null || data == null) {
                    if(continuation.isActive) continuation.resume(false)
                    return@dataTaskWithURL
                }

                val paths = NSSearchPathForDirectoriesInDomains(
                    directory = NSDocumentDirectory,
                    domainMask = NSUserDomainMask,
                    expandTilde = true
                )

                val documentsDirectory = paths.firstOrNull() as? String
                if (documentsDirectory == null) {
                    if(continuation.isActive) continuation.resume(false)
                    return@dataTaskWithURL
                }

                val filePath = "$documentsDirectory/$fileName"
                val fileUrl = NSURL.fileURLWithPath(path = filePath)

                val success = data.writeToURL(fileUrl, atomically = true)
                saveImageToPhotos(data)
                if(continuation.isActive) continuation.resume(success)
            }

            task.resume()
        }

        fun saveImageToPhotos(data: NSData) {
            val image = UIImage(data = data)
            UIImageWriteToSavedPhotosAlbum(image, null, null, null)
        }
    }
}
