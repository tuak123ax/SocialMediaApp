package com.minhtu.firesocialmedia.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance

class UiUtils {
    companion object{
        @Composable
        fun NewsCard(news: NewsInstance, context: Context, onNavigateToShowImageScreen: (image: String) -> Unit, onNavigateToUserInformation: (user: UserInstance?) -> Unit, homeViewModel: HomeViewModel) {
            val likeStatus by homeViewModel.likedPosts.collectAsState()
            val isLiked = likeStatus.contains(news.id)
            LaunchedEffect(likeStatus) {
                homeViewModel.updateLikeStatus()
            }
            val likeCountList = homeViewModel.likeCountList.collectAsState()
            val commentCountList = homeViewModel.commentCountList.collectAsState()
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .border(3.dp, Color.Gray),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.background(color = Color.Cyan).padding(10.dp).fillMaxWidth()
                            .clickable {
                                var user = Utils.findUserById(news.posterId, homeViewModel.listUsers)
                                if(user == null) {
                                    user = homeViewModel.currentUser
                                }
                                onNavigateToUserInformation(user)
                            }){
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(news.avatar))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster Avatar",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = news.posterName,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            Text(
                                text = Utils.convertTimeToDateString(news.timePosted),
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = news.message,
                        color = Color.Black,
                        modifier = Modifier.padding(10.dp) // Adds padding around text
                    )
                    if(news.image.isNotEmpty()){
                        AsyncImage(
                            model = news.image,
                            contentDescription = "Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(5.dp)
                                .clickable {
                                    onNavigateToShowImageScreen(news.image)
                                }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Start) {
                        Text(
                            text = "Like: ${likeCountList.value[news.id]}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Comment: ${commentCountList.value[news.id]}",
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                        Button(onClick = {
                            homeViewModel.clickLikeButton(news)
                        },
                            colors = if(isLiked) ButtonDefaults.buttonColors(Color.Cyan)
                            else ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.weight(1f)){
                            Image(
                                painter = painterResource(id = R.drawable.like),
                                contentDescription = "Like",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = if(isLiked) "Liked" else "Like", color = Color.Black)
                        }
                        Button(onClick = {
                            homeViewModel.clickCommentButton(news)
                        },
                            colors = ButtonDefaults.buttonColors(Color.White),
                            modifier = Modifier.weight(1f)){
                            Image(
                                painter = painterResource(id = R.drawable.comment),
                                contentDescription = "Comment",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Comment", color = Color.Black)
                        }
                    }
                }
            }
        }

    }
}