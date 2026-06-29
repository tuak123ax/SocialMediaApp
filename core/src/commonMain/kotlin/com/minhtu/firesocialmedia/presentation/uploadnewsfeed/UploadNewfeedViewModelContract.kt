package com.minhtu.firesocialmedia.presentation.uploadnewsfeed

import androidx.compose.runtime.MutableState
import com.minhtu.firesocialmedia.core.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.core.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.core.domain.entity.user.UserInstance
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract exposing the public API of UploadNewfeedViewModel.
 *
 * Lives in :shared so Navigation and UploadNewfeed UI in :shared can use it
 * without depending on :feature:home. Concrete impl lives in :feature:home.
 */
interface UploadNewfeedViewModelContract {
    var currentUser: UserInstance?
    fun updateCurrentUser(user: UserInstance)

    var message: String
    fun updateMessage(input: String)

    var image: String
    fun updateImage(input: String)

    var video: String
    fun updateVideo(input: String)

    var groupId: String
    fun updateGroupId(input: String)
    fun resetGroupId()

    val createPostStatus: StateFlow<Boolean?>
    val updatePostStatus: StateFlow<Boolean?>
    val postError: StateFlow<String?>
    fun resetPostError()

    val clickBackButton: StateFlow<Boolean>
    fun onClickBackButton()
    fun resetBackValue()

    val accessPermission: StateFlow<DecentralizationType>
    fun updateAccessPermission(permission: DecentralizationType)
    fun resetAccessPermission()

    fun createPost(user: UserInstance)
    fun resetPostStatus()
    fun updateNewInformation(new: NewsInstance)
    fun updatePostData(message: String, image: String, video: String)

    val newsPostedWhenOffline: StateFlow<List<NewsInstance>>
    val localPathOfSelectedDraft: MutableState<String>
    suspend fun loadNewsPostedWhenOffline()
    fun updateLocalPath(localPath: String)

    val deleteDraftStatus: StateFlow<Boolean?>
    fun resetDeleteDraftStatus()
    fun deleteAllDraftPosts()
    fun deleteDraftPost(newId: String)

    fun getGroupMembersFromGroupDetails(members: HashMap<String, String>)
}