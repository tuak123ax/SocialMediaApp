package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.domain.repository.ProfileFriendDbRepository

class ProfileFriendDbRepositoryImpl(
    private val databaseService: ProfileDatabaseService
) : ProfileFriendDbRepository {
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
