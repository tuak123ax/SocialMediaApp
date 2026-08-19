package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.friend.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.FriendDatabaseService
import com.minhtu.firesocialmedia.domain.repository.FriendDbRepository

class FriendDbRepositoryImpl(
    private val databaseService: FriendDatabaseService
) : FriendDbRepository {
    override suspend fun saveFriend(id: String, value: ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIENDS_PATH
        )
    }

    override suspend fun saveFriendRequest(id: String, value: ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIEND_REQUESTS_PATH
        )
    }
}
