package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import com.minhtu.firesocialmedia.domain.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.AppConfig
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

object SupabaseStorage {

    private const val BUCKET = "uploads"

    /**
     * Upload raw bytes to Supabase Storage at [remotePath] and return the public URL.
     */
    suspend fun uploadBytes(bytes: ByteArray, remotePath: String): String {
        val url = "${SupabaseClient.BASE_URL}storage/v1/object/$BUCKET/$remotePath"
        val response = KtorProvider.client.put(url) {
            header("Authorization", "Bearer ${AppConfig.supabaseApiKey}")
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }
        if (!response.status.isSuccess()) {
            throw Exception("Upload failed: ${response.status.value} ${response.status.description}")
        }
        return "${SupabaseClient.BASE_URL}storage/v1/object/public/$BUCKET/$remotePath"
    }

    /**
     * Upload a local file (by file URI string, e.g. "file:///path/to/video.mp4") to Supabase
     * Storage at [remotePath] and return the public URL.
     */
    suspend fun uploadFile(localPath: String, remotePath: String): String {
        val nsUrl = NSURL(string = localPath)
        val data = NSData.dataWithContentsOfURL(nsUrl)
            ?: throw Exception("Cannot read file at path: $localPath")
        val bytes = data.bytes!!.readBytes(data.length.toInt())
        return uploadBytes(bytes, remotePath)
    }

    /**
     * Delete the object at [remotePath] from Supabase Storage.
     */
    suspend fun delete(remotePath: String) {
        val url = "${SupabaseClient.BASE_URL}storage/v1/object/$BUCKET/$remotePath"
        val response = KtorProvider.client.delete(url) {
            header("Authorization", "Bearer ${AppConfig.supabaseApiKey}")
        }
        if (!response.status.isSuccess()) {
            throw Exception("Delete failed: ${response.status.value} ${response.status.description}")
        }
    }
}
