package com.minhtu.firesocialmedia.ios.service.serviceimpl.room

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance
import com.minhtu.firesocialmedia.data.local.service.room.HomeNewsRoomService

/**
 * No-op stub, matching the original (pre-Phase-3) `IosRoomService` convention: iOS Room access is
 * not wired up to a real database (see instruction.md).
 */
class IosNewsRoomService : HomeNewsRoomService {
    override suspend fun storeNewsToRoom(news: List<NewsInstance>) {
        // no-op
    }

    override suspend fun getFirstPage(number: Int): List<NewsInstance> {
        return emptyList()
    }

    override suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsInstance> {
        return emptyList()
    }

    override suspend fun getNewById(newId: String): NewsInstance? {
        return null
    }

    override suspend fun saveLikedPost(value: HashMap<String, Int>) {
        // no-op
    }

    override suspend fun getAllLikedPosts(): HashMap<String, Int> {
        return HashMap()
    }

    override suspend fun clearLikedPosts() {
        // no-op
    }

    override suspend fun hasLikedPost(): Boolean {
        return false
    }

    override suspend fun saveNews(new: NewsInstance) {
        // no-op
    }

    override suspend fun loadNewsPostedWhenOffline(): List<NewsInstance> {
        return emptyList()
    }

    override suspend fun deleteDraftPost(id: String) {
        // no-op
    }

    override suspend fun deleteAllDraftPosts() {
        // no-op
    }
}
