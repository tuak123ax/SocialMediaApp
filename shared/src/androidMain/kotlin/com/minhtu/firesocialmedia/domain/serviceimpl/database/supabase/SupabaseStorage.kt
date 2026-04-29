package com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase

import android.net.Uri
import com.minhtu.firesocialmedia.platform.AppConfig
import com.minhtu.firesocialmedia.platform.getAppContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object SupabaseStorage {

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
        val mediaType = mimeType.toMediaType()

        // Support both content:// URIs (media picker) and plain file paths
        val requestBody = if (filePath.startsWith("content://")) {
            val context = getAppContext()
            val bytes = context.contentResolver.openInputStream(Uri.parse(filePath))
                ?.use { it.readBytes() }
                ?: throw Exception("Cannot open content URI: $filePath")
            bytes.toRequestBody(mediaType)
        } else {
            File(filePath).asRequestBody(mediaType)
        }

        val response = SupabaseClient.api.uploadFile(
            bucket = BUCKET,
            path = remotePath,
            auth = "Bearer ${AppConfig.supabaseApiKey}",
            body = requestBody
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "(no body)"
            throw Exception("Upload failed: ${response.code()} ${response.message()} | body=$errorBody | path=$remotePath | mimeType=$mimeType")
        }

        return "${SupabaseClient.BASE_URL}storage/v1/object/public/$BUCKET/$remotePath"
    }

    suspend fun delete(remotePath: String) {
        val response = SupabaseClient.api.deleteFile(
            bucket = BUCKET,
            path = remotePath,
            auth = "Bearer ${AppConfig.supabaseApiKey}"
        )

        if (!response.isSuccessful) {
            throw Exception("Delete failed: ${response.code()} ${response.message()}")
        }
    }
}