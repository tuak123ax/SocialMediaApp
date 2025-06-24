package com.minhtu.firesocialmedia.utils

import androidx.compose.ui.graphics.Color
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class Utils {
    companion object{
        fun getAllCommentsOfNew(commentViewModel: CommentViewModel, newsId : String, platform: PlatformContext) {
            CoroutineScope(Dispatchers.IO).launch {
                platform.database.getAllComments(Constants.COMMENT_PATH, newsId, object : GetCommentCallback{
                    override fun onSuccess(comments: List<CommentInstance>) {
                        commentViewModel.listComments.clear()
                        commentViewModel.updateComments(ArrayList(comments))
                        commentViewModel.listComments.addAll(comments)
                        for(comment in commentViewModel.listComments) {
                            commentViewModel.addLikeCountData(comment.id, comment.likeCount)
                            if(comment.listReplies.isNotEmpty()){
                                commentViewModel.mapSubComments.putAll(comment.listReplies)
                            }
                        }
                    }

                    override fun onFailure() {
                    }

                })
            }
        }

        fun getAllFCMTokens(homeViewModel: HomeViewModel) : ArrayList<String>{
            val tokenList = ArrayList<String>()
            for(user in homeViewModel.listUsers) {
                if(user.token != homeViewModel.currentUser!!.token) {
                    tokenList.add(user.token)
                }
            }
            return tokenList
        }

        fun hexToColor(hex: String): Color {
            val cleanHex = hex.removePrefix("#")
            val colorLong = when (cleanHex.length) {
                6 -> "FF$cleanHex".toLong(16) // Add alpha if missing
                8 -> cleanHex.toLong(16)
                else -> throw IllegalArgumentException("Invalid hex color: $hex")
            }
            return Color(colorLong)
        }

        fun findUserById(userId : String, listUsers : List<UserInstance>) : UserInstance?{
            for(user in listUsers){
                if(user.uid == userId) {
                    return user
                }
            }
            return null
        }

        fun findNewById(newId : String, listNews : ArrayList<NewsInstance>) : NewsInstance?{
            for(new in listNews){
                if(new.id == newId) {
                    return new
                }
            }
            return null
        }

        suspend fun saveNotification(notification: NotificationInstance, friend : UserInstance, platform: PlatformContext) {
            //Save notification to friend's notification list
            try{
                friend.addNotification(notification)
                platform.database.saveNotificationToDatabase(friend.uid,
                    Constants.USER_PATH, friend.notifications)
            } catch(e: Exception) {
            }
        }

        suspend fun deleteNotification(notification: NotificationInstance, currentUser: UserInstance, platform: PlatformContext) {
            //Save notification to friend's notification list
            try{
                platform.database.deleteNotificationFromDatabase(currentUser.uid,
                    Constants.USER_PATH, notification)
            } catch(e: Exception) {
            }
        }
        interface GetUserCallback{
            fun onSuccess(users : List<UserInstance>)
            fun onFailure()
        }

        interface GetNewCallback{
            fun onSuccess(news : List<NewsInstance>)
            fun onFailure()
        }

        interface GetCommentCallback{
            fun onSuccess(comments : List<CommentInstance>)
            fun onFailure()
        }

        interface GetNotificationCallback{
            fun onSuccess(notifications : List<NotificationInstance>)
            fun onFailure()
        }

        interface SignInGoogleCallback{
            fun onSuccess(email : String)
            fun onFailure()
        }

        interface FetchSignInMethodCallback{
            fun onSuccess(result : Pair<Boolean, String>)
            fun onFailure(result : Pair<Boolean, String>)
        }

        interface SendPasswordResetEmailCallback{
            fun onSuccess()
            fun onFailure()
        }

        interface SaveSignUpInformationCallBack{
            fun onSuccess()
            fun onFailure()
        }

        interface BasicCallBack{
            fun onSuccess()
            fun onFailure()
        }
    }
}