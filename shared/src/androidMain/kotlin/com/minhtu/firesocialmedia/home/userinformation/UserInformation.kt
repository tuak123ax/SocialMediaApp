package com.minhtu.firesocialmedia.home.userinformation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.UiUtils

class UserInformation {
    companion object{
        @Composable
        fun UserInformationScreen(
            user: UserInstance?,
            paddingValues: PaddingValues,
            modifier: Modifier,
            homeViewModel : HomeViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
            onNavigateToHomeScreen : () -> Unit
        ){
            val newsList = homeViewModel.allNews.observeAsState(initial = emptyList())
            Box(modifier = modifier.padding(paddingValues)) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                    val context = LocalContext.current
                    //Cover photo
                    AsyncImage(model = ImageRequest.Builder(context)
                        .data(R.drawable.background)
                        .crossfade(true)
                        .build(),
                        contentDescription = "Cover photo",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth())
                    //User avatar, name and button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Ensures spacing between name and buttons
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = (-50).dp) // Moves avatar & name up
                        ) {
                            // User avatar
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(user?.image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape) // Ensures circular shape
                                    .border(2.dp, Color.White, CircleShape) // Optional border for better appearance
                            )

                            // User name with max width & ellipsis
                            Text(
                                text = user!!.name,
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 150.dp), // Restrict width to avoid touching buttons
                                overflow = TextOverflow.Ellipsis, // Add "..." if too long
                                maxLines = 1
                            )
                        }

                        // Move buttons up by adjusting offset(y = -20.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(y = (-20).dp) // Moves buttons up
                        ) {
                            // Chat button
                            IconButton(
                                onClick = { /* Handle click */ },
                                modifier = Modifier.border(1.dp, Color.Black, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = "Chat",
                                    tint = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp)) // Space between buttons

                            // Add friend button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp)) // Clip first before applying shadow
                                    .shadow(4.dp) // Apply shadow after clipping
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color.Red, Color.White)
                                        )
                                    )
                                    .clickable { }
                            ) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp)) // Ensures button shape
                                        .background(Color.Transparent), // Prevents default button background
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // Makes background follow Box
                                ) {
                                    Text("Add Friend", color = Color.Black, maxLines = 1)
                                }
                            }
                        }
                    }


                    LazyColumn(modifier = Modifier
                        .fillMaxSize().background(Color(0xFFE8E8E8))) {
                        val filterList = newsList.value.filter { news ->
                            (news.posterId == user!!.uid)
                        }
                        items(filterList){news ->
                            UiUtils.NewsCard(news = news, context, onNavigateToShowImageScreen, onNavigateToUserInformation, homeViewModel)
                        }
                    }
                }
                UiUtils.BackAndMoreOptionsRow(onNavigateToHomeScreen)
            }
        }
        fun getScreenName() : String {
            return "UserInformationScreen"
        }
    }
}