package com.minhtu.firesocialmedia.android.service.serviceimpl.room

import android.content.Context
import android.net.Uri
import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.data.local.dao.NewsDao
import com.minhtu.firesocialmedia.data.local.mapper.room.toDomain
import com.minhtu.firesocialmedia.data.local.mapper.room.toNewEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toRoomEntity
import com.minhtu.firesocialmedia.data.local.mapper.room.toDto
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService
import com.minhtu.firesocialmedia.platform.logMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidNewsRoomService(
    private val context: Context,
    private val newsDao: NewsDao
) : HomeNewsRoomService {
    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
        if (news.isNotEmpty()) {
            newsDao.addAll(news.toNewEntity())
        }
    }

    override suspend fun getFirstPage(number: Int): List<NewsInstance> {
        return newsDao.firstPage(number).toDomain()
    }

    override suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsInstance> {
        return newsDao.pageAfter(number, lastTimePosted, lastKey).toDomain()
    }

    override suspend fun getNewById(newId: String): NewsInstance? {
        return newsDao.getById(newId)?.toDomain()
    }

    override suspend fun saveLikedPost(value: HashMap<String, Int>) {
        newsDao.storeAllLikedPosts(value.toRoomEntity())
    }

    override suspend fun getAllLikedPosts(): HashMap<String, Int> {
        return newsDao.getAllLikedPosts().toDto()
    }

    override suspend fun clearLikedPosts() {
        newsDao.clearLikedPosts()
    }

    override suspend fun hasLikedPost(): Boolean {
        return newsDao.hasAnyLikedPosts()
    }

    override suspend fun saveNews(new: NewsInstance) {
        val entity = new.toRoomEntity().apply { isNewPost = true }
        if (entity.image.isNotEmpty()) {
            val file = copyPickedFileToAppStorage(Uri.parse(entity.image))
            entity.localPath = file.absolutePath
        }
        if (entity.video.isNotEmpty()) {
            logMessage("saveNews", { entity.video })
            val file = copyPickedFileToAppStorage(Uri.parse(entity.video))
            entity.localPath = file.absolutePath
            logMessage("saveNews", { "Local Path: " + entity.localPath })
        }
        newsDao.add(entity)
    }

    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> {
        return newsDao.loadNewsPostedWhenOffline().toDomain()
    }

    override suspend fun deleteDraftPost(id: String) {
        newsDao.deleteDraftPost(id)
    }

    override suspend fun deleteAllDraftPosts() {
        val numberDeletedFiles = deleteAllPickedFiles()
        logMessage("deleteAllDraftPosts", { "Number deleted files: $numberDeletedFiles" })
        newsDao.deleteAllDraftPosts()
    }

    suspend fun copyPickedFileToAppStorage(
        imageUri: Uri,
        directory: File = context.filesDir
    ): File = withContext(Dispatchers.IO) {
        val ext = context.contentResolver.getType(imageUri)?.substringAfterLast('/') ?: "jpg"
        val dst = File(directory, "picked_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            dst.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot open stream for $imageUri")
        dst
    }

    suspend fun deleteAllPickedFiles(
        directory: File = context.filesDir
    ): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        directory.listFiles()
            ?.filter { it.name.startsWith("picked_") }
            ?.forEach { file ->
                if (file.delete()) deletedCount++
            }
        deletedCount
    }
}
