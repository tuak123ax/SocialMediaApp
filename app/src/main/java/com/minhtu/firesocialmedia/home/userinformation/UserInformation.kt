package com.minhtu.firesocialmedia.home.userinformation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.UiUtils

class UserInformation {
    companion object{
        @Composable
        fun UserInformationScreen(
            user: UserInstance?,
            modifier: Modifier,
            homeViewModel : HomeViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
        ){
            val newsList = homeViewModel.allNews.observeAsState(initial = emptyList())
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(10.dp)) {
                val context = LocalContext.current
                AsyncImage(model = ImageRequest.Builder(context)
                    .data(user?.image)
                    .crossfade(true)
                    .build(),
                    contentDescription = "Chat App Icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .clickable {

                        })
                Text(
                    text = user!!.name,
                    color = Color.Black,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.padding(20.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
                    .fillMaxSize()) {
                    val filterList = newsList.value.filter { news ->
                        (news.posterId == user.uid)
                    }
                    items(filterList){news ->
                        UiUtils.NewsCard(news = news, context, onNavigateToShowImageScreen, onNavigateToUserInformation, homeViewModel)
                    }
                }
            }
        }
        fun getScreenName() : String {
            return "UserInformationScreen"
        }
    }
}