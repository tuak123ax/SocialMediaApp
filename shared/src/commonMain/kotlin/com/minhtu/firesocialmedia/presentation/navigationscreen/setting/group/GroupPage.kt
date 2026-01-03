package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.core.DecentralizationType
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformation.Companion.DropdownMenuForCoverPhoto
import com.minhtu.firesocialmedia.utils.UiUtils
import com.seiko.imageloader.ui.AutoSizeImage

class GroupPage {
    companion object {
        @Composable
        fun GroupPageScreen(
            imagePicker: ImagePicker,
            group: GroupInstance,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            modifier: Modifier,
            homeViewModel : HomeViewModel,
            groupPageViewModel: GroupPageViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
            onNavigateBack : () -> Unit,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit,
            onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit
        ){
            val listState = rememberLazyListState()
            val newsList = homeViewModel.allNews.collectAsState()
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }

            val commentStatus by homeViewModel.commentStatus.collectAsState()
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }

            Box(modifier = modifier.padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    //Cover photo box
                    Box(contentAlignment = Alignment.Center) {
                        var showMenu by remember { mutableStateOf(false) }
                        val coverPhotoModifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .clickable {
                                showMenu = true
                            }
                            .testTag(TestTag.TAG_COVER_PHOTO)
                            .semantics {
                                contentDescription = TestTag.TAG_COVER_PHOTO
                            }
                        val imageBytes = produceState<ByteArray?>(
                            initialValue = null,
                            groupPageViewModel.coverPhoto
                        ) {
                            value =
                                if (groupPageViewModel.coverPhoto == Constants.DEFAULT_AVATAR_URL) {
                                    getImageBytesFromDrawable("unknownavatar")
                                } else {
                                    imagePicker.loadImageBytes(groupPageViewModel.coverPhoto)
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
                            false,
                            { onNavigateToShowImageScreen(groupPageViewModel.coverPhoto) },
                            { imagePicker.pickImage() },
                            { showMenu = false })
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
                                localImageLoaderValue
                            ) {
                                AutoSizeImage(
                                    group.avatar,
                                    contentDescription = "image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape) // Ensures circular shape
                                        .border(
                                            2.dp,
                                            Color.White,
                                            CircleShape
                                        ) // Optional border for better appearance
                                        .testTag(TestTag.TAG_USER_AVATAR)
                                        .semantics {
                                            contentDescription = TestTag.TAG_USER_AVATAR
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp)) // Space between avatar and name
                            // User name with max width & ellipsis
                            Text(
                                text = group.name,
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
                            Spacer(modifier = Modifier.width(8.dp)) // Space between buttons
                        }
                    }

                    val filterList by remember {
                        derivedStateOf {
                            newsList.value
                                .filter { news ->
                                    (news.groupId == group.id)
                                }
                                .filter { news ->
                                    when(news.decentralizationType) {
                                        DecentralizationType.Private -> news.posterId == homeViewModel.currentUser?.uid
                                        else -> true
                                    }
                                }
                        }
                    }
                    UiUtils.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                        localImageLoaderValue,
                        listState,
                        homeViewModel,
                        filterList,
                        onNavigateToUploadNewsfeed,
                        onNavigateToShowImageScreen,
                        onNavigateToUserInformation,
                        showBottomSheet = { news ->
                            newToBeShared = news
                            showBottomSheet = true
                        }
                    )
                }
                UiUtils.BackAndMoreOptionsRow(onNavigateBack)
                if(showBottomSheet) {
                    UiUtils.ShareBottomSheet(
                        deepLink = "https://firechat-aa433.web.app/news/${newToBeShared?.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onClick = {
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
        fun getScreenName() : String {
            return "GroupPageScreen"
        }
    }
}