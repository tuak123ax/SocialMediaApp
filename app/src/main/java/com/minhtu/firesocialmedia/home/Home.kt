package com.minhtu.firesocialmedia.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance

class Home {
    companion object{
        var listUsers: ArrayList<UserInstance>? = ArrayList()
        var listNews: ArrayList<NewsInstance>? = ArrayList()
        var currentUser : UserInstance? = null
        @Composable
        fun HomeScreen(modifier: Modifier,
                       homeViewModel: HomeViewModel = viewModel(),
                       onNavigateToUploadNews: () -> Unit){
            val context = LocalContext.current
            val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
            LaunchedEffect(lifecycleOwner.value) {
                getAllUsers(homeViewModel)
                getAllNews(homeViewModel)
            }

            val openChatAppIntent = getChatAppIntent(context)
            val openChatAppLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                
            }

            //Observe Live Data as State
            val usersList =  homeViewModel.allUsers.observeAsState(initial = emptyList())

            val newsList = homeViewModel.allNews.observeAsState(initial = emptyList())
            Column(verticalArrangement = Arrangement.Top, modifier = modifier) {
                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    AsyncImage(model = R.drawable.fire_chat_icon,
                        contentDescription = "Chat App Icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (openChatAppIntent != null) {
                                    openChatAppLauncher.launch(openChatAppIntent)
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Can't find this app on your device!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            })

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "FireNotebook",
                        color = Color.Cyan,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AsyncImage(model = R.drawable.search,
                        contentDescription = "Chat App Icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {

                            })
                }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){
                    OutlinedTextField(
                        value = homeViewModel.message, onValueChange = {
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .clickable {
                                onNavigateToUploadNews()
                            },
                        label = { Text(text = "What are you thinking?")},
                        enabled = false,    // Disables the TextField
                        singleLine = true
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)) {
                    items(usersList.value){user ->
                        UserCard(user = user, context)
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
                    .fillMaxSize()) {
                    items(newsList.value){news ->
                        NewsCard(news = news, context)
                    }
                }
            }
        }

        private fun getAllUsers(homeViewModel: HomeViewModel) {
            val currentUserId = FirebaseAuth.getInstance().uid
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("users")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listUsers!!.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val user: UserInstance? = dataSnapshot.getValue(UserInstance::class.java)
                        if (user != null) {
                            if(user.uid != currentUserId) {
                                listUsers!!.add(user)
                            } else {
                                currentUser = user
                            }
                        }
                    }
                    homeViewModel.updateUsers(listUsers!!)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun getAllNews(homeViewModel: HomeViewModel) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("news")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listNews!!.clear()
                    for (dataSnapshot in snapshot.getChildren()) {
                        val news: NewsInstance? = dataSnapshot.getValue(NewsInstance::class.java)
                        if (news != null) {
                            listNews!!.add(news)
                        }
                    }
                    homeViewModel.updateNews(listNews!!)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        @Composable
        private fun UserCard(user: UserInstance, context: Context) {
            Card(
                modifier = Modifier.size(100.dp, 120.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(user.image))
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .weight(1f) // Allocates equal space to the image and text
                            .clickable {
                                // Handle image click
                            }
                    )
                    Spacer(modifier = Modifier.height(1.dp)) // Optional spacing between image and text
                    Text(
                        text = user.name,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis, // Adds "..." at the end if the text overflows
                        modifier = Modifier.padding(horizontal = 4.dp) // Adds padding around text
                    )
                }
            }
        }

        @Composable
        private fun NewsCard(news: NewsInstance, context: Context) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.padding(20.dp).fillMaxWidth()){
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(news.avatar))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable {
                                    // Handle image click
                                }
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = news.posterName,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 2.dp) // Adds padding around text
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = news.message,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 4.dp) // Adds padding around text
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    AsyncImage(
                        model = news.image,
                        contentDescription = "Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }

        private fun getChatAppIntent(context: Context): Intent? {
            val packageName = "com.example.firechat"
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if(intent != null) {
                return intent
            }
            return null
        }

        fun getScreenName(): String{
            return "HomeScreen"
        }
    }
}