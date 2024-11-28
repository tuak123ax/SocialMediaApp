package com.minhtu.firesocialmedia.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance

class Utils {
    companion object{
        fun getAllNews(homeViewModel: HomeViewModel) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("news")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    homeViewModel.listNews!!.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val news: NewsInstance? = dataSnapshot.getValue(NewsInstance::class.java)
                        if (news != null) {
                            homeViewModel.listNews!!.add(news)
                        }
                    }
                    homeViewModel.updateNews(homeViewModel.listNews!!)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}