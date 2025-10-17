package com.minhtu.firesocialmedia.data.repository

import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.DatabaseService
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.repository.CommonDbRepository

class CommonDbRepositoryImpl(
    private val databaseService: DatabaseService
) : CommonDbRepository {
    override suspend fun saveLikedPost(
        id: String,
        value: HashMap<String, Int>
    ): Boolean {
        return databaseService.saveValueToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.LIKED_POSTS_PATH
        )
    }

    override suspend fun saveInstanceToDatabase(
        id: String,
        instance: BaseNewsInstance) : Boolean {
        return databaseService.saveInstanceToDatabase(
            id,
            DataConstant.NEWS_PATH,
            instance
        )
    }

    override suspend fun saveCommentToDatabase(
        selectedNewId: String,
        commentId : String,
        instance : BaseNewsInstance
    ): Boolean {
        return databaseService.saveInstanceToDatabase(
            commentId,
            DataConstant.NEWS_PATH+"/"+selectedNewId+"/"+ DataConstant.COMMENT_PATH,
            instance
        )
    }

    override suspend fun saveSubCommentToDatabase(
        id: String,
        selectedNewId : String,
        parentCommentId : String,
        instance: BaseNewsInstance
    ): Boolean {
        return databaseService.saveInstanceToDatabase(
            id,
            DataConstant.NEWS_PATH+"/"+selectedNewId+"/"+ DataConstant.COMMENT_PATH+"/"+parentCommentId+"/"+ DataConstant.LIST_REPLIES_PATH,
            instance
        )
    }

    override suspend fun deleteCommentFromDatabase(
        selectedNewId: String,
        instance: BaseNewsInstance
    ) {
        databaseService.deleteCommentFromDatabase(
            DataConstant.NEWS_PATH + "/" +
                    selectedNewId + "/" +
                    DataConstant.COMMENT_PATH,
            instance
        )
    }

    override suspend fun deleteSubCommentFromDatabase(
        selectedNewId: String,
        parentCommentId : String,
        instance: BaseNewsInstance
    ) {
        databaseService.deleteCommentFromDatabase(
            DataConstant.NEWS_PATH + "/" +
                    selectedNewId + "/" +
                    DataConstant.COMMENT_PATH + "/" +
                    parentCommentId + "/" +
                    DataConstant.LIST_REPLIES_PATH,
            instance
        )
    }

    override suspend fun updateCommentCountForNewInDatabase(
        id: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_COUNT_PATH,
            value
        )
    }

    override suspend fun updateReplyCountForCommentInDatabase(
        id: String,
        currentCommentId : String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + currentCommentId +"/"
                    +DataConstant.COMMENT_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForNewInDatabase(
        id: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            id,
            DataConstant.NEWS_PATH,
            DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, commentId : String, value: Int) {
        databaseService.updateCountValueInDatabase(
            selectedNewId,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" + commentId + "/" + DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun updateLikeCountForSubCommentInDatabase(
        selectedNewId: String,
        parentCommentId : String,
        likedComment: String,
        value: Int
    ) {
        databaseService.updateCountValueInDatabase(
            selectedNewId,
            DataConstant.NEWS_PATH,
            DataConstant.COMMENT_PATH + "/" +
                    parentCommentId + "/" +
                    DataConstant.LIST_REPLIES_PATH + "/" +
                    likedComment + "/" +
                    DataConstant.LIKED_COUNT_PATH,
            value
        )
    }

    override suspend fun saveLikedComments(
        id: String,
        value: HashMap<String, Int>
    ): Boolean {
        return databaseService.saveValueToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.LIKED_COMMENT_PATH
        )
    }

    override suspend fun saveFriend(id : String,
                                    value : ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIENDS_PATH
        )
    }

    override suspend fun saveFriendRequest(id : String,
                                           value : ArrayList<String>) {
        databaseService.saveListToDatabase(
            id,
            DataConstant.USER_PATH,
            value,
            DataConstant.FRIEND_REQUESTS_PATH
        )
    }
}