package com.minhtu.firesocialmedia.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.services.notification.Client
import com.minhtu.firesocialmedia.services.notification.NotificationApiService
import com.minhtu.firesocialmedia.services.notification.NotificationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class Utils {
    companion object{
         fun getAllUsers(homeViewModel: HomeViewModel, context: Context) {
            val currentUserId = FirebaseAuth.getInstance().uid
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child(Constants.USER_PATH)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    homeViewModel.listUsers.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val user: UserInstance? = dataSnapshot.getValue(UserInstance::class.java)
                        if (user != null) {
                            if(user.uid != currentUserId) {
                                homeViewModel.listUsers.add(user)
                            } else {
                                homeViewModel.updateCurrentUser(user, context)
                            }
                        }
                    }
                    homeViewModel.updateUsers(homeViewModel.listUsers)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
        fun getAllNews(homeViewModel: HomeViewModel) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child(Constants.NEWS_PATH)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    homeViewModel.listNews.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val news: NewsInstance? = dataSnapshot.getValue(NewsInstance::class.java)
                        if (news != null) {
                            Log.e("getAllNews", "news: ${news.id}")
                            homeViewModel.listNews.add(news)
                            homeViewModel.addLikeCountData(news.id, news.likeCount)
                            homeViewModel.addCommentCountData(news.id, news.commentCount)
                        }
                    }
                    homeViewModel.updateNews(homeViewModel.listNews)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun getAllCommentsOfNew(commentViewModel: CommentViewModel, newsId : String) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child(Constants.NEWS_PATH).child(newsId).child(
                Constants.COMMENT_PATH)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentViewModel.listComments.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val comments: CommentInstance? = dataSnapshot.getValue(CommentInstance::class.java)
                        if (comments != null) {
                            commentViewModel.listComments.add(comments)
                        }
                    }
                    commentViewModel.updateComments(commentViewModel.listComments)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
        fun generateRandomId(): String {
            return UUID.randomUUID().toString()
        }

        fun findUserById(userId : String, listUsers : ArrayList<UserInstance>) : UserInstance?{
            for(user in listUsers){
                if(user.uid == userId) {
                    return user
                }
            }
            return null
        }

        fun getCurrentTime() : Long{
            //Get current time in milliseconds
            return System.currentTimeMillis()
        }

        fun convertTimeToDateString(time : Long) : String{
            //Convert time in milliseconds to date string
            return SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(time))
        }

        fun createMessageForServer(message: String, tokenList : ArrayList<String>, sender : UserInstance): NotificationRequest {
            val body = JSONObject()
            try {
                val tokens = JSONArray()
                for(token in tokenList) {
                    tokens.put(token)
                }
                val data = JSONObject()
                data.put(Constants.KEY_FCM_TOKEN, sender.token)
                data.put(Constants.KEY_USER_ID, sender.uid)
                data.put(Constants.KEY_AVATAR, sender.image)
                data.put(Constants.KEY_EMAIL, sender.email)
                data.put(Constants.REMOTE_MSG_TITLE, sender.name)
                data.put(Constants.REMOTE_MSG_BODY, message)

                body.put(Constants.REMOTE_MSG_DATA, data)
                body.put(Constants.REMOTE_MSG_TOKENS, tokens)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return NotificationRequest(body)
        }

        fun sendMessageToServer(request: NotificationRequest) {
            Log.e("sendMessageToServer",request.data.toString())
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = Client.getClient(Constants.APP_SCRIPT_URL)?.create(NotificationApiService::class.java)!!
                        .sendToAppScript(request.data.toString()).execute()
                    if (response.isSuccessful) {
                        Log.d("FCM", "Notification Sent Successfully: ${response.body()}")
                    } else {
                        Log.e("FCM", "Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Exception: ${e.message}")
                }
            }
        }

         fun updateTokenInStorage(token: String, context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val secureSharedPreferences = CryptoHelper.getEncryptedSharedPreferences(context)
                secureSharedPreferences.edit().putString(Constants.KEY_FCM_TOKEN, token).apply()
            }
        }
    }
}