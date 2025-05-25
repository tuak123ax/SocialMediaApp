package com.minhtu.firesocialmedia.utils

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
         fun getAllUsers(homeViewModel: HomeViewModel, platform: PlatformContext) {
             val currentUserId = platform.auth.getCurrentUserUid()
             CoroutineScope(Dispatchers.IO).launch {
                 platform.database.getAllUsers(Constants.USER_PATH, object : GetUserCallback{
                     override fun onSuccess(users: List<UserInstance>) {
                         val currentUser = findUserById(currentUserId!!, users)
                         homeViewModel.updateCurrentUser(currentUser!!, platform)
                         val listAllUsers = ArrayList(users)
                         listAllUsers.remove(currentUser)
                         homeViewModel.updateUsers(listAllUsers)
                         homeViewModel.listUsers.clear()
                         homeViewModel.listUsers.addAll(listAllUsers)
                         getAllNotificationsOfUser(homeViewModel, platform)
                     }

                     override fun onFailure() {
                     }
                 })
             }
        }
        fun getAllNews(homeViewModel: HomeViewModel, platform: PlatformContext) {
            CoroutineScope(Dispatchers.IO).launch{
                platform.database.getAllNews(Constants.NEWS_PATH, object : GetNewCallback{
                    override fun onSuccess(news: List<NewsInstance>) {
                        homeViewModel.updateNews(ArrayList(news))
                        homeViewModel.listNews.clear()
                        for(new in news) {
                            homeViewModel.listNews.add(new)
                            homeViewModel.addLikeCountData(new.id, new.likeCount)
                            homeViewModel.addCommentCountData(new.id, new.commentCount)
                        }
                    }

                    override fun onFailure() {
                    }

                })
            }
        }

        fun getAllCommentsOfNew(commentViewModel: CommentViewModel, newsId : String, platform: PlatformContext) {
            CoroutineScope(Dispatchers.IO).launch {
                platform.database.getAllComments(Constants.COMMENT_PATH, newsId, object : GetCommentCallback{
                    override fun onSuccess(comments: List<CommentInstance>) {
                        commentViewModel.listComments.clear()
                        commentViewModel.updateComments(ArrayList(comments))
                        commentViewModel.listComments.addAll(comments)
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

        fun getAllNotificationsOfUser(homeViewModel: HomeViewModel, platform: PlatformContext) {
            val currentUserId = platform.auth.getCurrentUserUid()
            CoroutineScope(Dispatchers.IO).launch{
                platform.database.getAllNotificationsOfUser(Constants.NOTIFICATION_PATH, currentUserId!!, object : GetNotificationCallback{
                    override fun onSuccess(notifications: List<NotificationInstance>) {
                        homeViewModel.listNotificationOfCurrentUser.clear()
                        homeViewModel.listNotificationOfCurrentUser.addAll(notifications)
                        homeViewModel.updateNotifications(ArrayList(homeViewModel.listNotificationOfCurrentUser.toList()))
                    }

                    override fun onFailure() {
                    }

                })
            }
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
    }
}