package com.minhtu.firesocialmedia.data.local.service.room

import com.minhtu.firesocialmedia.home.entity.news.NewsInstance

/**
 * Room-backed local storage contract for feature/home's News domain — both the NewsInstance-typed
 * feed/paging/draft-post methods and the LikedPost primitive map (they're backed by the same
 * NewsDao/underlying table, and share the same platform implementation, so they live on a single
 * interface rather than being split across two).
 *
 * feature/profile also needs the LikedPost subset (UserRepositoryImpl.saveLikedPost) without a
 * Gradle dependency on feature/home — that's served by feature/profile's own
 * [com.minhtu.firesocialmedia.data.local.service.room.LikedPostRoomService], bridged to this same
 * underlying storage by appInit (which already depends on every feature).
 */
interface HomeNewsRoomService {
    suspend fun storeNewsToRoom(news: List<NewsInstance>)
    suspend fun getFirstPage(number: Int): List<NewsInstance>
    suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsInstance>
    suspend fun getNewById(newId: String): NewsInstance?
    suspend fun saveNews(new: NewsInstance)
    suspend fun loadNewsPostedWhenOffline(): List<NewsInstance>
    suspend fun deleteDraftPost(id: String)
    suspend fun deleteAllDraftPosts()

    suspend fun saveLikedPost(value: HashMap<String, Int>)
    suspend fun getAllLikedPosts(): HashMap<String, Int>
    suspend fun clearLikedPosts()
    suspend fun hasLikedPost(): Boolean
}
