package com.minhtu.firesocialmedia.presentation.userinformation

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.model.NewsInstance
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.ImagePicker
import com.minhtu.firesocialmedia.platform.generateImageLoader
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.navigationscreen.friend.FriendViewModel
import com.minhtu.firesocialmedia.utils.NavigationHandler
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.Utils
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.ui.AutoSizeImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
                val relationship =
                    userInformationViewModel.checkRelationship(user!!, homeViewModel.currentUser!!)
                userInformationViewModel.updateRelationship(relationship)
            }

            LaunchedEffect(Unit) {
                friendViewModel.updateFriendRequests(homeViewModel.currentUser!!.friendRequests)
                friendViewModel.updateFriends(homeViewModel.currentUser!!.friends)
            }

            Box(modifier = modifier.padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.Start
                ) {
                    //Cover photo box
                    Box(contentAlignment = Alignment.Companion.Center) {
                        var showMenu by remember { mutableStateOf(false) }
                        val coverPhotoModifier = Modifier.Companion
                            .height(200.dp)
                            .fillMaxWidth()
                            .clickable {
                                showMenu = true
                            }
                            .testTag(TestTag.Companion.TAG_COVER_PHOTO)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_COVER_PHOTO
                            }
                        var imageBytes = produceState<ByteArray?>(
                            initialValue = null,
                            userInformationViewModel.coverPhoto
                        ) {
                            value =
                                if (userInformationViewModel.coverPhoto == Constants.Companion.DEFAULT_AVATAR_URL) {
                                    getImageBytesFromDrawable("unknownavatar")
                                } else {
                                    imagePicker.loadImageBytes(userInformationViewModel.coverPhoto)
                                }
                        }
                        if (imageBytes.value != null) {
                            imagePicker.ByteArrayImage(
                                imageBytes.value,
                                modifier = coverPhotoModifier
                            )
                        }
                        DropdownMenuForCoverPhoto(
                            showMenu,
                            isCurrentUser,
                            { onNavigateToShowImageScreen(userInformationViewModel.coverPhoto) },
                            { imagePicker.pickImage() },
                            { showMenu = false })
                    }
                    //User avatar, name and button
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Ensures spacing between name and buttons
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Companion.CenterHorizontally,
                            modifier = Modifier.Companion
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
                                    contentScale = ContentScale.Companion.Crop,
                                    modifier = Modifier.Companion
                                        .size(120.dp)
                                        .clip(CircleShape) // Ensures circular shape
                                        .border(
                                            2.dp,
                                            Color.Companion.White,
                                            CircleShape
                                        ) // Optional border for better appearance
                                        .testTag(TestTag.Companion.TAG_USER_AVATAR)
                                        .semantics {
                                            contentDescription = TestTag.Companion.TAG_USER_AVATAR
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.Companion.height(10.dp)) // Space between avatar and name
                            // User name with max width & ellipsis
                            Text(
                                text = user!!.name,
                                color = Color.Companion.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Companion.Bold,
                                textAlign = TextAlign.Companion.Center,
                                modifier = Modifier.Companion.widthIn(max = 150.dp), // Restrict width to avoid touching buttons
                                overflow = TextOverflow.Companion.Ellipsis, // Add "..." if too long
                                maxLines = 1
                            )
                        }

                        // Move buttons up by adjusting offset(y = -20.dp)
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            modifier = Modifier.Companion.offset(y = (-20).dp) // Moves buttons up
                        ) {
                            // Chat button
                            IconButton(
                                onClick = { /* Handle click */ },
                                modifier = Modifier.Companion.border(
                                    1.dp,
                                    Color.Companion.Black,
                                    CircleShape
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = "Chat",
                                    tint = Color.Companion.Gray
                                )
                            }

                            Spacer(modifier = Modifier.Companion.width(8.dp)) // Space between buttons

                            // Add friend button
                            Box(
                                contentAlignment = Alignment.Companion.Center,
                                modifier = Modifier.Companion
                                    .clip(RoundedCornerShape(20.dp)) // Clip first before applying shadow
                                    .shadow(4.dp) // Apply shadow after clipping
                                    .background(
                                        Brush.Companion.linearGradient(
                                            colors = listOf(
                                                Color.Companion.Red,
                                                Color.Companion.White
                                            )
                                        )
                                    )
                            ) {
                                var showMenu by remember { mutableStateOf(false) }
                                Button(
                                    onClick = {
                                        if (addFriendStatus != Relationship.WAITING_RESPONSE) {
                                            val relationship =
                                                userInformationViewModel.checkRelationship(
                                                    user!!,
                                                    homeViewModel.currentUser!!
                                                )
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
                                    modifier = Modifier.Companion
                                        .clip(
                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                20.dp
                                            )
                                        ) // Ensures button shape
                                        .background(Color.Companion.Transparent) // Prevents default button background
                                        .testTag(TestTag.Companion.TAG_BUTTON_ADDFRIEND)
                                        .semantics {
                                            contentDescription =
                                                TestTag.Companion.TAG_BUTTON_ADDFRIEND
                                        },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.Transparent) // Makes background follow Box
                                ) {
                                    Text(
                                        text = when (addFriendStatus) {
                                            Relationship.FRIEND -> "Unfriend"
                                            Relationship.FRIEND_REQUEST -> "Cancel Request"
                                            Relationship.NONE -> "Add Friend"
                                            Relationship.WAITING_RESPONSE -> "Response"
                                            else -> "Unknown"
                                        },
                                        color = Color.Companion.Black,
                                        maxLines = 1
                                    )
                                }
                                DropdownMenuForResponse(
                                    platform,
                                    showMenu,
                                    friendViewModel,
                                    userInformationViewModel,
                                    user!!,
                                    homeViewModel.currentUser!!,
                                    { showMenu = false })
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
                    UiUtils.Companion.LazyColumnOfNewsWithSlideOutAnimation(
                        platform,
                        homeViewModel,
                        filterList,
                        onNavigateToUploadNewsfeed,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation
                    )
                }
                UiUtils.Companion.BackAndMoreOptionsRow(navController)
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
                val friend = Utils.Companion.findUserById(friendId, homeViewModel.listUsers)
                if(friend != null) {
                    friendTokens.add(friend.token)
                }
            }
            return friendTokens
        }
    }
}