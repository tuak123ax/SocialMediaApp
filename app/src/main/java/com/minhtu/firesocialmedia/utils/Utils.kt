package com.minhtu.firesocialmedia.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.comment.CommentViewModel
import com.minhtu.firesocialmedia.instance.CommentInstance
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import java.util.UUID

class Utils {
    companion object{
         fun getAllUsers(homeViewModel: HomeViewModel) {
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
                                Log.e("Home", "set current user: "+ user.name)
                                homeViewModel.currentUser = user
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
                            homeViewModel.listNews.add(news)
                        }
                    }
                    homeViewModel.updateNews(homeViewModel.listNews)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun getAllCommentsOfNew(commentViewModel: CommentViewModel, newsId : String) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child(Constants.NEWS_PATH).child(newsId).child(Constants.COMMENT_PATH)
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

        fun generateRandomId(): String {
            return UUID.randomUUID().toString()
        }
    }
}