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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.userinformation.UserInformation.Companion.DropdownMenuForCoverPhoto
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.LazyColumnOfNewsWithSlideOutAnimationAndLoadMore
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.SearchUserCard
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ShareBottomSheet
import com.minhtu.firesocialmedia.utils.Utils.Companion.convertToNumberString
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
            loadingViewModel : LoadingViewModel,
            groupDetailsViewModel: GroupDetailsViewModel,
            onNavigateToShowImageScreen : (image : String) -> Unit,
            onNavigateToUserInformation : (user : UserInstance?) -> Unit,
            onNavigateBack : () -> Unit,
            onNavigateToUploadNewsfeed: (updateNew : NewsInstance?) -> Unit,
            onNavigateToCommentScreen: (selectedNew : NewsInstance) -> Unit,
            onClickInviteButton : () -> Unit,
            onLeaveGroup : () -> Unit,
            onManageMembers : (GroupInstance) -> Unit
        ){
            CommonBackHandler{
                onNavigateBack()
            }
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

            val isMember = if(fetchGroupInfoState == null) false else fetchGroupInfoState!!.members.containsKey(currentUser.uid)

            val joinGroupState by groupDetailsViewModel.joinGroupStatus.collectAsState()
            LaunchedEffect(joinGroupState) {
                if(joinGroupState != null) {
                    if(joinGroupState!!) {
                        showToast("Join group successfully!!!")
                        //Fetch group info again to get new data
                        groupDetailsViewModel.fetchGroupInfo(groupId)
                    } else {
                        showToast("Cannot join this group now. Please retry!")
                    }
                    groupDetailsViewModel.resetJoinGroupState()
                }
            }

            val showAlertDialog = remember { mutableStateOf(false) }
            UiUtils.ShowDiscardDialog(
                    "Leave Group",
            "Are you sure you want to leave this group?",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                onDiscard = {
                if(fetchGroupInfoState != null) {
                    val adminSet = fetchGroupInfoState!!.members.filterValues {it == "admin"}.keys
                    val memberSet = fetchGroupInfoState!!.members.filterValues {it == "member"}.keys
                    val isAdmin = adminSet.contains(currentUser.uid)
                    if(isAdmin && adminSet.size <= 1 && memberSet.isNotEmpty()) {
                        showToast("You are the last admin in the group. Cannot leave!")
                    } else {
                        groupDetailsViewModel.leaveGroup(
                            currentUser,
                            fetchGroupInfoState!!
                        )
                    }
                }
            },
                showDialog = showAlertDialog
            )
            val leaveGroupStatus by groupDetailsViewModel.leaveGroupStatus.collectAsState()
            LaunchedEffect(leaveGroupStatus) {
                if(leaveGroupStatus != null) {
                    if(leaveGroupStatus!!) {
                        showToast("Leave group successfully!!!")
                        onLeaveGroup()
                    } else {
                        showToast("Cannot leave this group now. Please retry!")
                    }
                    groupDetailsViewModel.resetLeaveGroupStatus()
                }
            }

            var showPasswordDialog by remember { mutableStateOf(false) }


            //Show more options menu
            var showMoreOptionsMenu by remember { mutableStateOf(false) }
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
                                val isAdmin = fetchGroupInfoState?.members
                                    ?.filterValues { it == "admin" }
                                    ?.keys
                                    ?.contains(currentUser.uid) == true
                                DropdownMenuForCoverPhoto(
                                    showMenu,
                                    isAdmin,
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
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                .testTag(TestTag.TAG_SELECT_GROUP_AVATAR)
                                                .semantics {
                                                    contentDescription = TestTag.TAG_SELECT_GROUP_AVATAR
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if(fetchGroupInfoState != null) {
                                                AutoSizeImage(
                                                    fetchGroupInfoState!!.avatar.toStorageUrl(),
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
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Group,
                                                        contentDescription = "Placeholder",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            color = MaterialTheme.colorScheme.onSurface,
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
                                            color = MaterialTheme.colorScheme.onSurface,
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
                                        if(fetchGroupInfoState != null) {
                                            Text(
                                                text = if(fetchGroupInfoState!!.password.isNotEmpty()) "Private Group" else "Public Group",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        } else {
                                            Text(
                                                text = "Fetching...",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                    if(fetchGroupInfoState!= null && fetchGroupInfoState!!.description.isNotEmpty()) {
                                        Text(
                                            text = fetchGroupInfoState!!.description,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                //Only show invite button and notification setting for members
                                if(isMember) {
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
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ){
                                            Text(
                                                text = "Invite",
                                                color = MaterialTheme.colorScheme.surface
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
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                    contentColor = MaterialTheme.colorScheme.onSurface
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
                            }

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
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
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if(fetchGroupInfoState != null) {
                        if(isMember) {
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
                                    groupDetailsViewModel,
                                    loadingViewModel,
                                    onNavigateToShowImageScreen,
                                    onNavigateToUserInformation,
                                    onNavigateToUploadNewsfeed
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    if(fetchGroupInfoState != null) {
                                        if(fetchGroupInfoState!!.password.isNotEmpty()) {
                                            showPasswordDialog = true
                                        } else {
                                            groupDetailsViewModel.joinGroup(currentUser, fetchGroupInfoState!!)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Join group",
                                    color = MaterialTheme.colorScheme.surface,
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
                UiUtils.BackAndTitleAndMoreOptionsRow(
                    "Group Details",
                    trailingIcon = "more_horiz",
                    showMoreOptionsMenu = showMoreOptionsMenu,
                    isMember = isMember,
                    isAdmin = if(fetchGroupInfoState != null) fetchGroupInfoState!!.members[currentUser.uid] == "admin" else false,
                    navigateBack = onNavigateBack,
                    onClickMoreOptions = {
                        showMoreOptionsMenu = true
                    },
                    onDismissRequest = {
                        showMoreOptionsMenu = false
                    },
                    onLeaveGroup = {
                        if(fetchGroupInfoState != null) {
                            showAlertDialog.value = true
                        }
                    },
                    onManageMembers = {
                        if(fetchGroupInfoState != null) {
                            onManageMembers(fetchGroupInfoState!!)
                        }
                    }
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
                if(showPasswordDialog) {
                    PasswordDialog(
                        show = showPasswordDialog,
                        onDismiss = { showPasswordDialog = false },
                        onConfirm = { password ->
                            if(fetchGroupInfoState!= null && fetchGroupInfoState!!.password == password) {
                                groupDetailsViewModel.joinGroup(currentUser, fetchGroupInfoState!!)
                                showPasswordDialog = false
                            } else {
                                showToast("Password is wrong. Please try again!!!")
                            }
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = eventName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
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
            groupDetailsViewModel : GroupDetailsViewModel,
            loadingViewModel: LoadingViewModel,
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
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        indicator = {
                                tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.error
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                group.posts.values.toList().sortedByDescending { it.timePosted },
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
                            val isLoading by loadingViewModel.isLoading.collectAsState()
                            var memberList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                            // Run filtering when friend list or search query changes
                            LaunchedEffect(Unit) {
                                loadingViewModel.showLoading()
                                memberList = coroutineScope {
                                    group.members.keys.map { userId ->
                                        async {
                                            homeViewModel.findUserById(userId)
                                        }
                                    }.awaitAll().filterNotNull()
                                }
                                loadingViewModel.hideLoading()
                            }

                            Box {
                                LazyColumn(
                                    modifier = Modifier
                                        .testTag(TestTag.TAG_MEMBERS_TAB)
                                        .semantics { contentDescription = TestTag.TAG_MEMBERS_TAB }
                                ) {
                                    items(memberList) { user ->
                                        SearchUserCard(
                                            user,
                                            localImageLoaderValue,
                                            onClickViewProfileButton = {
                                                onNavigateToUserInformation(user)
                                            }
                                        )
                                    }
                                }
                                if(isLoading) {
                                    Loading.LoadingScreen()
                                }
                            }
                        }
                        2 -> {
                            val gridState = rememberLazyGridState()
                            val listImages = groupDetailsViewModel.visibleImages
                            // Init once per group
                            LaunchedEffect(group.id) {
                                groupDetailsViewModel.init(group.posts.values.toList())
                            }
                            // React only to new posts
                            LaunchedEffect(group.posts.size) {
                                groupDetailsViewModel.onPostsUpdated(group.posts.values.toList())
                            }
                            LaunchedEffect(gridState) {
                                snapshotFlow {
                                    gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                }.collect { index ->
                                    if (index != null && index >= listImages.size - 6) {
                                        groupDetailsViewModel.loadMore()
                                    }
                                }
                            }
                            GroupImageGalleryScreen(
                                images = listImages,
                                localImageLoaderValue = localImageLoaderValue,
                                onImageClick = { image ->
                                    onNavigateToShowImageScreen(image)
                                }
                            )
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
                        avatar.toStorageUrl(),
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

        @Composable
        fun DropdownMenuForMoreOptionsInGroup(expanded : Boolean,
                                              isAdmin : Boolean = false,
                                              onLeaveGroup : () -> Unit,
                                              onDismissRequest: () -> Unit,
                                              onManageMembers : () -> Unit) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("Leave group") },
                    onClick = {
                        onLeaveGroup()
                        onDismissRequest()
                    }
                )
                if(isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Manage members") },
                        onClick = {
                            onManageMembers()
                            onDismissRequest()
                        }
                    )
                }
            }
        }

        @Composable
        fun GroupImageGalleryScreen(
            images: List<String>,
            localImageLoaderValue : ProvidedValue<*>,
            onImageClick: (String) -> Unit
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images) { image ->
                    GalleryImageItem(
                        imageUrl = image,
                        localImageLoaderValue,
                        onClick = { onImageClick(image) }
                    )
                }
            }
        }

        @Composable
        fun GalleryImageItem(
            imageUrl: String,
            localImageLoaderValue : ProvidedValue<*>,
            onClick: () -> Unit
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // square image
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }
            ) {
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        imageUrl.toStorageUrl(),
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        @Composable
        fun PasswordDialog(
            show: Boolean,
            onDismiss: () -> Unit,
            onConfirm: (String) -> Unit
        ) {
            if (!show) return

            var password by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Enter password") },
                text = {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(password)
                            password = ""
                        },
                        enabled = password.isNotBlank()
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}