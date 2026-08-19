package com.minhtu.firesocialmedia.android.service.serviceimpl.database.supabase.auth

import android.net.Uri
import com.minhtu.firesocialmedia.android.service.serviceimpl.notification.KtorProvider
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.getAppContext
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.File

object SupabaseStorage {

    private const val BASE_URL = "https://pcklhkkafpomfvhboini.supabase.co/"
    private const val BUCKET = "uploads"

    suspend fun upload(filePath: String, remotePath: String): String {
        val mimeType = when (remotePath.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            "webp"        -> "image/webp"
            "mp4"         -> "video/mp4"
            "mov"         -> "video/quicktime"
            else          -> "image/jpeg"
        }

        val bytes = if (filePath.startsWith("content://")) {
            val context = getAppContext()
            context.contentResolver.openInputStream(Uri.parse(filePath))
                ?.use { it.readBytes() }
                ?: throw Exception("Cannot open content URI: $filePath")
        } else {
            File(filePath).readBytes()
        }

        val url = "${BASE_URL}storage/v1/object/$BUCKET/$remotePath"
        val response = KtorProvider.client.post(url) {
            header("Authorization", "Bearer ${AppConfig.supabaseApiKey}")
            header("x-upsert", "true")
            contentType(ContentType.parse(mimeType))
            setBody(bytes)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Upload failed: ${response.status.value} ${response.status.description} | path=$remotePath | mimeType=$mimeType")
        }

        return "${BASE_URL}storage/v1/object/public/$BUCKET/$remotePath"
    }

    suspend fun delete(remotePath: String) {
        val url = "${BASE_URL}storage/v1/object/$BUCKET/$remotePath"
        val response = KtorProvider.client.delete(url) {
            header("Authorization", "Bearer ${AppConfig.supabaseApiKey}")
        }
        if (!response.status.isSuccess()) {
            throw Exception("Delete failed: ${response.status.value} ${response.status.description}")
        }
    }
}
