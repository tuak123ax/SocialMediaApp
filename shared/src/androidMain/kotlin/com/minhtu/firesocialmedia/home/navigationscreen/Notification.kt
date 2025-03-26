package com.minhtu.firesocialmedia.home.navigationscreen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.search.SearchViewModel
import com.minhtu.firesocialmedia.instance.UserInstance

class Notification {
    companion object{
        @Composable
        fun NotificationScreen(modifier: Modifier,
                         paddingValues: PaddingValues,
                         searchViewModel: SearchViewModel = viewModel(),
                         homeViewModel: HomeViewModel,
                         onNavigateToUserInformation: (user : UserInstance) -> Unit,
                         onNavigateToShowImageScreen: (image : String) -> Unit){
            Column(verticalArrangement = Arrangement.Top,horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(paddingValues)) {
                val context = LocalContext.current
                Text(text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
                LazyColumn {
                    val filterList = homeViewModel.listUsers.filter { user ->
                        user.name.contains(searchViewModel.query, ignoreCase = true)
                    }
                    items(filterList){user ->
                        NotificationRow(user, context, onNavigateToUserInformation)
                    }
                }
            }
        }

        @Composable
        fun NotificationRow(user : UserInstance, context : Context, onNavigateToEventDestination: (user: UserInstance) -> Unit) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToEventDestination(user)
                    }){
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(user.image))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(10.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = user.name,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 5.dp) // Adds padding around text
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Handle click */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz, //Horizontal three dots ⋯
                        contentDescription = "More Options",
                        tint = Color.Black
                    )
                }
            }
        }

        fun getScreenName() : String {
            return "NotificationScreen"
        }
    }
}