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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.ImagePicker
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.generateImageLoader
import com.minhtu.firesocialmedia.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.home.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.instance.NewsInstance
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.launch

class UserInformation {
    companion object{
        @Composable
        fun UserInformationScreen(
            platform : PlatformContext,
            imagePicker: ImagePicker,
            user: UserInstance?,
            isCurrentUser : Boolean,
            paddingValues: PaddingValues,
            modifier: Modifier,
            homeViewModel : HomeViewModel,
            friendViewModel: FriendViewModel,
            userInformationViewModel: UserInformationViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
            navController : NavigationHandler,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit
        ){
            val newsList = homeViewModel.allNews.collectAsState()
            val addFriendStatus by userInformationViewModel.addFriendStatus.collectAsState()
            LaunchedEffect(Unit) {
                val relationship = userInformationViewModel.checkRelationship(user!!, homeViewModel.currentUser!!)
                userInformationViewModel.updateRelationship(relationship)
            }

            LaunchedEffect(Unit) {
                friendViewModel.updateFriendRequests(homeViewModel.currentUser!!.friendRequests)
                friendViewModel.updateFriends(homeViewModel.currentUser!!.friends)
            }

            Box(modifier = modifier.padding(paddingValues)) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                    //Cover photo box
                    Box(contentAlignment = Alignment.Center){
                        var showMenu by remember { mutableStateOf(false) }
                        val coverPhotoModifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .clickable {
                                showMenu = true
                            }
                            .testTag(TestTag.TAG_COVER_PHOTO)
                            .semantics{
                                contentDescription = TestTag.TAG_COVER_PHOTO
                            }
                        var imageBytes = produceState<ByteArray?>(initialValue = null, userInformationViewModel.coverPhoto) {
                            value = if(userInformationViewModel.coverPhoto == Constants.DEFAULT_AVATAR_URL) {
                                getImageBytesFromDrawable("unknownavatar")
                            } else {
                                imagePicker.loadImageBytes(userInformationViewModel.coverPhoto)
                            }
                        }
                        if(imageBytes.value != null) {
                            imagePicker.ByteArrayImage(
                                imageBytes.value,
                                modifier = coverPhotoModifier
                            )
                        }
                        DropdownMenuForCoverPhoto(
                            showMenu,
                            isCurrentUser,
                            {onNavigateToShowImageScreen(userInformationViewModel.coverPhoto)},
                            {imagePicker.pickImage()},
                            {showMenu = false})
                    }
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
                            modifier = Modifier
                                .weight(1f)
                                .offset(y = (-50).dp) // Moves avatar & name up
                        ) {
                            // User avatar
                            CompositionLocalProvider(
                                LocalImageLoader provides remember { generateImageLoader() },
                            ) {
                                AutoSizeImage(
                                    user!!.image,
                                    contentDescription = "image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape) // Ensures circular shape
                                        .border(2.dp, Color.White, CircleShape) // Optional border for better appearance
                                        .testTag(TestTag.TAG_USER_AVATAR)
                                        .semantics{
                                            contentDescription = TestTag.TAG_USER_AVATAR
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp)) // Space between avatar and name
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
                            ) {
                                var showMenu by remember { mutableStateOf(false) }
                                Button(
                                    onClick = {
                                        if(addFriendStatus != Relationship.WAITING_RESPONSE) {
                                            val relationship = userInformationViewModel.checkRelationship(user!!, homeViewModel.currentUser!!)
                                            userInformationViewModel.updateRelationship(relationship)
                                            userInformationViewModel.clickAddFriendButton(
                                                friend = user,
                                                currentUser = homeViewModel.currentUser,
                                                platform
                                            )
                                        } else {
                                            showMenu = true
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp)) // Ensures button shape
                                        .background(Color.Transparent) // Prevents default button background
                                        .testTag(TestTag.TAG_BUTTON_ADDFRIEND)
                                        .semantics{
                                            contentDescription = TestTag.TAG_BUTTON_ADDFRIEND
                                        },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // Makes background follow Box
                                ) {
                                    Text(text = when(addFriendStatus){
                                        Relationship.FRIEND -> "Unfriend"
                                        Relationship.FRIEND_REQUEST -> "Cancel Request"
                                        Relationship.NONE -> "Add Friend"
                                        Relationship.WAITING_RESPONSE -> "Response"
                                        else -> "Unknown"
                                    },
                                        color = Color.Black,
                                        maxLines = 1)
                                }
                                DropdownMenuForResponse(platform, showMenu, friendViewModel, userInformationViewModel, user!!, homeViewModel.currentUser!!,{showMenu = false} )
                            }
                        }
                    }

                    val filterList by remember {
                        derivedStateOf {
                            newsList.value.filter { news ->
                                (news.posterId == user!!.uid)
                            }
                        }
                    }
                    UiUtils.LazyColumnOfNewsWithSlideOutAnimation(
                        platform,
                        homeViewModel,
                        filterList,
                        onNavigateToUploadNewsfeed,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation)
                }
                UiUtils.BackAndMoreOptionsRow(navController)
            }
        }

        @Composable
        fun DropdownMenuForResponse(platform : PlatformContext,
                                    expanded : Boolean,
                                    friendViewModel: FriendViewModel,
                                    userInformationViewModel: UserInformationViewModel,
                                    requester : UserInstance,
                                    currentUser : UserInstance,
                                    onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Accept") },
                    onClick = {
                        // Handle Accept action
                        friendViewModel.acceptFriendRequest(requester, currentUser, platform)
                        userInformationViewModel.updateRelationship(Relationship.FRIEND)
                        onDismissRequest()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Reject") },
                    onClick = {
                        // Handle Reject action
                        friendViewModel.rejectFriendRequest(requester, currentUser, platform)
                        userInformationViewModel.updateRelationship(Relationship.NONE)
                        onDismissRequest()
                    }
                )
            }
        }

        fun getScreenName() : String {
            return "UserInformationScreen"
        }

        @Composable
        fun DropdownMenuForCoverPhoto(expanded : Boolean,
                                      isCurrentUser : Boolean,
                                      onViewCoverPhoto : () -> Unit,
                                      onChangeCoverPhoto : () -> Unit,
                                      onDismissRequest: () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("View cover photo") },
                    onClick = {
                        onViewCoverPhoto()
                        onDismissRequest()
                    }
                )
//                if(isCurrentUser) {
//                    DropdownMenuItem(
//                        text = { Text("Change cover photo") },
//                        onClick = {
//                            onChangeCoverPhoto()
//                            onDismissRequest()
//                        }
//                    )
//                }
            }
        }

        private fun getAllFriendTokens(homeViewModel: HomeViewModel) : ArrayList<String> {
            val friendTokens = ArrayList<String>()
            val friendIds = homeViewModel.currentUser!!.friends
            for(friendId in friendIds) {
                val friend = Utils.findUserById(friendId, homeViewModel.listUsers)
                if(friend != null) {
                    friendTokens.add(friend.token)
                }
            }
            return friendTokens
        }
    }
}