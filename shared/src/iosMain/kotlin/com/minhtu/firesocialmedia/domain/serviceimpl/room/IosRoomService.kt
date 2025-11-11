package com.minhtu.firesocialmedia.domain.serviceimpl.room

import com.minhtu.firesocialmedia.data.local.entity.CommentEntity
import com.minhtu.firesocialmedia.data.local.entity.LikedPostEntity
import com.minhtu.firesocialmedia.data.local.entity.NewsEntity
import com.minhtu.firesocialmedia.data.local.entity.NotificationEntity
import com.minhtu.firesocialmedia.data.local.entity.UserEntity
import com.minhtu.firesocialmedia.data.local.service.room.RoomService

class IosRoomService : RoomService {
    override suspend fun storeUserFriendsToRoom(friends: List<UserEntity?>) {
        // no-op on iOS for now
    }

    override suspend fun storeUserFriendToRoom(friend: UserEntity) {
        // no-op on iOS for now
    }

    override suspend fun storeNewsToRoom(news: List<NewsEntity>) {
        // no-op on iOS for now
    }

    override suspend fun storeNotificationsToRoom(notifications: List<NotificationEntity>) {
        // no-op on iOS for now
    }

    override suspend fun getUserFromRoom(userId: String): UserEntity? {
        return null
    }

    override suspend fun getAllNotifications(): List<NotificationEntity> {
        return emptyList()
    }

    override suspend fun getFirstPage(number: Int): List<NewsEntity> {
        return emptyList()
    }

    override suspend fun getPageAfter(number: Int, lastTimePosted: Long, lastKey: String?): List<NewsEntity> {
        return emptyList()
    }

    override suspend fun getNewById(newId: String): NewsEntity? {
        return null
    }

    override suspend fun saveLikedPost(value: List<LikedPostEntity>) {
        // no-op on iOS for now
    }

    override suspend fun getAllLikedPosts(): List<LikedPostEntity> {
        return emptyList()
    }

    override suspend fun clearLikedPosts() {
        // no-op on iOS for now
    }

    override suspend fun saveComment(commentEntity: CommentEntity) {
        // no-op on iOS for now
    }

    override suspend fun getAllComments(): List<CommentEntity> {
        return emptyList()
    }

    override suspend fun clearComments() {
        // no-op on iOS for now
    }

    override suspend fun hasLikedPost(): Boolean {
        return false
    }

    override suspend fun hasComment(): Boolean {
        return false
    }

    override suspend fun saveNews(new: NewsEntity) {
        // no-op on iOS for now
    }

    override suspend fun loadNewsPostedWhenOffline(): List<NewsEntity> {
        return emptyList()
    }

    override suspend fun deleteDraftPost(id: String) {
        // no-op on iOS for now
    }

    override suspend fun deleteAllDraftPosts() {
        // no-op on iOS for now
    }
}

