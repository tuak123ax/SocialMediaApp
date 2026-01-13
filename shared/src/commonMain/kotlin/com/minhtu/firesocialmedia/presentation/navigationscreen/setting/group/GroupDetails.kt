package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.news.NewsInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformation.Companion.DropdownMenuForCoverPhoto
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ShareBottomSheet
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.UserRow
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class GroupDetails {
    companion object Companion {
        @Composable
        fun GroupDetailsScreen(
            currentUser : UserInstance,
            imagePicker: ImagePicker,
            groupId: String,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            modifier: Modifier = Modifier,
            homeViewModel : HomeViewModel,
            searchViewModel : SearchViewModel,
            groupDetailsViewModel: GroupDetailsViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
            onNavigateBack : () -> Unit,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit,
            onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
            onClickInviteButton : () -> Unit
        ){
            // Preserve scroll position across navigation/back stack using rememberSaveable
            val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(0, 0) }
            var isGroupInfoVisible by remember { mutableStateOf(true) }
            var userInteracted by remember { mutableStateOf(false) }
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by remember { mutableStateOf<NewsInstance?>(null) }

            //Load group information
            val fetchGroupInfoState by groupDetailsViewModel.fetchGroupInfoState.collectAsState()
            val notificationStatus by groupDetailsViewModel.notificationState.collectAsState()
            LaunchedEffect(Unit){
                groupDetailsViewModel.fetchGroupInfo(groupId)
                groupDetailsViewModel.fetchNotificationState(currentUser.uid, groupId)
            }

            val commentStatus by homeViewModel.commentStatus.collectAsState()
            LaunchedEffect(commentStatus) {
                commentStatus?.let { selectedNew ->
                    onNavigateToCommentScreen(selectedNew)
                    homeViewModel.resetCommentStatus()
                }
            }

            // LaunchedEffect to track the scroll state (hide top bar and show load more)
            LaunchedEffect(listState) {
                snapshotFlow {
                    val layoutInfo = listState.layoutInfo
                    val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = layoutInfo.totalItemsCount
                    val inProgress = listState.isScrollInProgress

                    Triple(firstVisible, lastVisible, totalItems) to inProgress
                }
                    .distinctUntilChanged()
                    .collectLatest { (triple, state) ->
                        val (firstVisible, lastVisible, totalItems) = triple
                        val inProgress = state
                        if(inProgress && firstVisible > 0) {
                            userInteracted = true
                        }
                        // Show/hide top bar
                        isGroupInfoVisible = if(!userInteracted) {
                            true
                        } else {
                            firstVisible == 0
                        }
                    }
            }

            val updateNotificationState by groupDetailsViewModel.updateNotificationState.collectAsState()
            LaunchedEffect(updateNotificationState) {
                if(updateNotificationState != null) {
                    if(!updateNotificationState!!) {
                        showToast("Cannot change notification status now. Please retry!")
                        //Back to the old state
                        groupDetailsViewModel.updateNotificationStateWhenClickButton()
                    }
                }
            }

            Box(modifier = modifier.padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    //Column contains group info and will be dismissed when scroll down
                    AnimatedVisibility(visible = isGroupInfoVisible) {
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
                                    groupDetailsViewModel.coverPhoto
                                ) {
                                    value =
                                        if (groupDetailsViewModel.coverPhoto == Constants.DEFAULT_AVATAR_URL) {
                                            getImageBytesFromDrawable("unknownavatar")
                                        } else {
                                            imagePicker.loadImageBytes(groupDetailsViewModel.coverPhoto)
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
                                    { onNavigateToShowImageScreen(groupDetailsViewModel.coverPhoto) },
                                    { imagePicker.pickImage() },
                                    { showMenu = false })
                            }
                            //Avatar, name and button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween // Ensures spacing between name and buttons
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .offset(y = (-40).dp)
                                        .padding(start = 10.dp)
                                ) {
                                    // User avatar
                                    CompositionLocalProvider(
                                        localImageLoaderValue
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.White, CircleShape)
                                                .testTag(TestTag.TAG_SELECT_GROUP_AVATAR)
                                                .semantics {
                                                    contentDescription = TestTag.TAG_SELECT_GROUP_AVATAR
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if(fetchGroupInfoState != null) {
                                                AutoSizeImage(
                                                    fetchGroupInfoState!!.avatar,
                                                    contentDescription = "image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .matchParentSize() // ensures same size
                                                        .clip(CircleShape)
                                                )
                                            } else {
                                                // Placeholder
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .background(
                                                            color = Color.LightGray.copy(alpha = 0.3f),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Group,
                                                        contentDescription = "Placeholder",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }
                                        }

                                    }
                                    Spacer(modifier = Modifier.height(10.dp)) // Space between avatar and name
                                    // User name with max width & ellipsis
                                    if(fetchGroupInfoState != null) {
                                        Text(
                                            text = fetchGroupInfoState!!.name,
                                            color = Color.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(max = 150.dp), // Restrict width to avoid touching buttons
                                            overflow = TextOverflow.Ellipsis, // Add "..." if too long
                                            maxLines = 1
                                        )
                                    } else {
                                        Text(
                                            text = "Fetching...",
                                            color = Color.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(max = 150.dp), // Restrict width to avoid touching buttons
                                            overflow = TextOverflow.Ellipsis, // Add "..." if too long
                                            maxLines = 1
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ){
                                        CrossPlatformIcon(
                                            icon = "global",
                                            backgroundColor = "#FFFFFFFF",
                                            contentDescription = "Global",
                                            modifier = Modifier
                                                .size(25.dp)
                                                .padding(end = 5.dp)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        if(fetchGroupInfoState != null) {
                                            Text(
                                                text = if(fetchGroupInfoState!!.password.isNotEmpty()) "Private Group" else "Public Group",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        } else {
                                            Text(
                                                text = "Fetching...",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                    if(fetchGroupInfoState!= null && fetchGroupInfoState!!.description.isNotEmpty()) {
                                        Text(
                                            text = fetchGroupInfoState!!.description,
                                            color = Color.Black,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            //Click invite button
                                            onClickInviteButton()
                                        },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Red
                                        )
                                    ){
                                        Text(
                                            text = "Invite",
                                            color = Color.White
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    if(notificationStatus != null) {
                                        Button(
                                            onClick = {
                                                groupDetailsViewModel.updateNotificationStatus(
                                                    groupId,
                                                    currentUser.uid)
                                            },
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, Color.LightGray),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = Color.Black
                                            ),
                                            modifier = Modifier.size(35.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            if(notificationStatus!!) {
                                                Icon(
                                                    Icons.Default.Notifications,
                                                    contentDescription = "Notification"
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.NotificationsOff,
                                                    contentDescription = "Notification"
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            //Additional info
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp).padding(vertical = 10.dp)
                            ){
                                NumberAndEventCard(
                                    if(fetchGroupInfoState != null) fetchGroupInfoState!!.members.size else 0,
                                    "MEMBERS"
                                )
                                NumberAndEventCard(
                                    if(fetchGroupInfoState != null) fetchGroupInfoState!!.posts.size else 0,
                                    "POSTS"
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                        }
                    }

                    if(fetchGroupInfoState != null) {
                        if(fetchGroupInfoState!!.members.containsKey(currentUser.uid)) {
                            //Column to show posts in group
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                TabLayoutForGroup(
                                    currentUser,
                                    fetchGroupInfoState!!,
                                    listState,
                                    listOf("Feed", "Members", "Photos"),
                                    localImageLoaderValue,
                                    homeViewModel,
                                    onNavigateToShowImageScreen,
                                    onNavigateToUserInformation,
                                    onNavigateToUploadNewsfeed
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {

                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.LightGray),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Join group",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                UiUtils.BackAndMoreOptionsRow(onNavigateBack)
                Text(
                    text = "GroupDetails",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
                if(showBottomSheet) {
                    ShareBottomSheet(
                        deepLink = "${DataConstant.DEEP_LINK}/news/${newToBeShared?.id}",
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
            return "GroupDetailsScreen"
        }

        @Composable
        fun NumberAndEventCard(number : Int,
                               eventName : String,
                               modifier: Modifier = Modifier
        ) {
            Column(
                modifier = modifier
            ) {
                Text(
                    text = convertToNumberString(number),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = eventName,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        fun convertToNumberString(number : Int) : String{
            return if(number < 1000) {
                number.toString()
            } else if(number < 1000000) {
                (number/1000).toString() + "K"
            } else if(number < 1000000000) {
                (number/1000000).toString() + "M"
            } else {
                (number/1000000000).toString() + "M"
            }
        }

        @Composable
        fun TabLayoutForGroup(
            currentUser : UserInstance,
            group : GroupInstance,
            listState: LazyListState,
            tabTitles : List<String>,
            localImageLoaderValue : ProvidedValue<*>,
            homeViewModel: HomeViewModel,
            onNavigateToShowImageScreen: (image: String) -> Unit,
            onNavigateToUserInformation: (user: UserInstance?) -> Unit,
            onNavigateToUploadNewsfeed : (updateNew : NewsInstance?) -> Unit){
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            var newToBeShared by mutableStateOf<NewsInstance?>(null)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()){
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        indicator = {
                                tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color.Red
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed{
                                index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 18.sp,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            )
                        }
                    }
                    when(selectedTabIndex){
                        0 -> {
                            AvatarAndEditTextRow(
                                currentUser.image,
                                localImageLoaderValue,
                                onNavigateToUploadNewsfeed
                            )
                            LazyColumnOfNewsWithSlideOutAnimationAndLoadMore(
                                localImageLoaderValue,
                                listState,
                                homeViewModel,
                                group.posts.values.toList(),
                                onNavigateToUploadNewsfeed,
                                onNavigateToShowImageScreen,
                                onNavigateToUserInformation,
                                showBottomSheet = { news ->
                                    newToBeShared = news
                                    showBottomSheet = true
                                }
                            )
                        }
                        1 -> {
                            var memberList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                            // Run filtering when friend list or search query changes
                            LaunchedEffect(Unit) {
                                memberList = coroutineScope {
                                    group.members.keys.map { userId ->
                                        async {
                                            homeViewModel.findUserById(userId)
                                        }
                                    }.awaitAll().filterNotNull()
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .testTag(TestTag.TAG_MEMBERS_TAB)
                                    .semantics { contentDescription = TestTag.TAG_MEMBERS_TAB }
                            ) {
                                items(memberList) { user ->
                                    UserRow(user, localImageLoaderValue, onNavigateToUserInformation)
                                }
                            }
                        }
                        2 -> {

                        }
                    }
                }
                if(showBottomSheet) {
                    ShareBottomSheet(
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
        @Composable
        fun AvatarAndEditTextRow(
            avatar : String,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateToUploadNews: (updateNew : NewsInstance?) -> Unit,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        avatar,
                        contentDescription = "Poster Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(vertical = 5.dp)
                            .padding(start = 10.dp)
                            .clip(CircleShape)
                            .testTag(TestTag.TAG_CURRENT_USER)
                            .semantics {
                                contentDescription =
                                    TestTag.TAG_CURRENT_USER
                            }
                    )
                }

                //Create post
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .clickable { onNavigateToUploadNews(null) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Write something to the group...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}