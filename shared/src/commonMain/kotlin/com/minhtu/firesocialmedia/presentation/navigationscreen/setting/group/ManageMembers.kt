package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.sharedmodule.ui.theme.adminBorderColor
import com.minhtu.sharedmodule.ui.theme.adminCardColor
import com.minhtu.sharedmodule.ui.theme.memberCardColor
import com.minhtu.sharedmodule.ui.theme.positiveBackgroundButtonColor
import com.minhtu.sharedmodule.ui.theme.positiveTintColor
import com.minhtu.firesocialmedia.storage.toStorageUrl
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.delay

class ManageMembers {
    companion object{
        @OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
        @Composable
        fun ManageMembersScreen(
            currentUser : UserInstance,
            group : GroupInstance,
            manageMembersViewModel: ManageMembersViewModel,
            searchViewModel : SearchViewModel,
            loadingViewModel: LoadingViewModel,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            onNavigateBack : () -> Unit,
            onInviteMembers : () -> Unit,
            onNavigateToUserInformationScreen : (UserInstance) -> Unit,
            onNavigateToSelectGroupScreen : () -> Unit
        ) {
            CommonBackHandler {
                onNavigateBack()
            }
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val removeMemberStatus by manageMembersViewModel.removeMemberStatus.collectAsState()
            var adminToDelete by remember { mutableStateOf<UserInstance?>(null) }
            val showAlertDialog = remember { mutableStateOf(false) }
            val adminSet = group.members.filterValues {it == "admin"}.keys
            val memberSet = group.members.filterValues {it == "member"}.keys
            val adminList by manageMembersViewModel.fetchAdminListStatus.collectAsState()
            val memberList by manageMembersViewModel.fetchMemberListStatus.collectAsState()
            //Selected member to be added or removed from list when promoting or demoting
            var selectedMember by remember { mutableStateOf<UserInstance?>(null) }
            var adminPendingDeleteId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                manageMembersViewModel.fetchAdminList(adminSet)
                manageMembersViewModel.fetchMemberList(memberSet)
            }

            LaunchedEffect(removeMemberStatus) {
                if(removeMemberStatus != null) {
                    if(removeMemberStatus!!) {
                        //You have removed yourself from group.
                        if(adminToDelete != null && adminToDelete!!.uid == currentUser.uid) {
                            showToast("You have removed yourself from group!!!")
                            onNavigateToSelectGroupScreen()
                            manageMembersViewModel.resetAdminAndMemberList()
                        } else {
                            showToast("Remove successfully!!!")
                        }
                    } else {
                        showToast("Cannot remove this person now. Please try again!!!")
                    }
                    adminToDelete = null
                    manageMembersViewModel.resetRemoveMemberStatus()
                }
            }

            val promoteMemberStatus by manageMembersViewModel.promoteMemberStatus.collectAsState()
            LaunchedEffect(promoteMemberStatus) {
                if(promoteMemberStatus != null) {
                    if(promoteMemberStatus!!) {
                        showToast("Promote member successfully!!!")
                        if(selectedMember != null) {
                            manageMembersViewModel.removeMemberFromList(selectedMember!!)
                            manageMembersViewModel.addAdminToList(selectedMember!!)
                        }
                    } else {
                        showToast("Cannot promote member now. Please try again!!!")
                    }
                    manageMembersViewModel.resetPromoteMemberStatus()
                    delay(400) // match animateItemPlacement duration
                    selectedMember = null
                }
            }

            val demoteMemberStatus by manageMembersViewModel.demoteMemberStatus.collectAsState()
            LaunchedEffect(demoteMemberStatus) {
                if(demoteMemberStatus != null) {
                    if(demoteMemberStatus!!) {
                        showToast("Demote member successfully!!!")
                        if(selectedMember != null) {
                            manageMembersViewModel.removeAdminFromList(selectedMember!!)
                            manageMembersViewModel.addMemberToList(selectedMember!!)
                        }
                    } else {
                        showToast("Cannot Demote member now. Please try again!!!")
                    }
                    manageMembersViewModel.resetDemoteMemberStatus()
                    delay(400) // match animateItemPlacement duration
                    selectedMember = null
                }
            }
            Box(modifier = Modifier
                .background(Color.White)
                .padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Manage Members",
                        trailingIcon = "add_member",
                        trailingIconTint = Color.Red,
                        navigateBack = {
                            onNavigateBack()
                            manageMembersViewModel.resetAdminAndMemberList()
                        },
                        onClickMoreOptions = {
                            onInviteMembers()
                        }
                    )
                    Search.SearchBar(
                        query = searchViewModel.query,
                        onQueryChange = { query -> searchViewModel.updateQuery(query) },
                        modifier = Modifier.height(80.dp).padding(vertical = 10.dp)
                            .testTag(TestTag.TAG_SEARCH_BAR)
                            .semantics {
                                contentDescription = TestTag.TAG_SEARCH_BAR
                            }
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        thickness = 1.dp,
                        color = Color.LightGray
                    )

                    SharedTransitionLayout {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().background(Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            // ───── Admin section ─────
                            item { SectionHeader(
                                header = "Admins",
                                subHeader = if(adminList.size > 1) "${adminList.size} Admins" else "${adminList.size} Admin",
                                headerTextColor = Color.Red
                            )}
                            val filterAdminList = adminList.filter {
                                it.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                            items(filterAdminList, key = {it.uid}) { admin ->
                                val visible = adminPendingDeleteId != admin.uid
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .background(Color.White)
                                        .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing
                                        )
                                        )
                                ) {
                                    this@Column.AnimatedVisibility(
                                        visible = visible,
                                        exit = slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                    ) {
                                        AnimatedMemberItem(
                                            admin.uid,
                                            selectedMember?.uid
                                        ) {
                                            MemberCard(
                                                admin,
                                                "admin",
                                                localImageLoaderValue,
                                                onClickInfoButton = {
                                                    onNavigateToUserInformationScreen(admin)
                                                },
                                                onClickPromoteButton = {
                                                    //Click this button on an admin means demote
                                                    if(adminList.size > 1) {
                                                        selectedMember = admin
                                                        manageMembersViewModel.demoteMember(
                                                            admin,
                                                            group
                                                        )
                                                    } else {
                                                        showToast("Cannot demote the last admin of group!!!")
                                                    }
                                                },
                                                onClickRemoveButton = {
                                                    adminToDelete = admin
                                                    showAlertDialog.value = true
                                                },
                                                modifier = Modifier
                                            )
                                        }
                                    }
                                }
                            }

                            // ───── Member section ─────
                            item { SectionHeader(
                                header = "Members",
                                subHeader = if(memberList.size > 1) "${memberList.size} Members" else "${memberList.size} Member",
                                headerTextColor = Color.Gray
                            )}
                            val filterMemberList = memberList.filter {
                                it.name.contains(searchViewModel.query, ignoreCase = true)
                            }
                            items(filterMemberList, key = {it.uid}) { member ->
                                //State to track visibility of a member
                                var visible by remember { mutableStateOf(true) }
                                //State to track to delay before delete data from db
                                var pendingDelete by remember { mutableStateOf(false) }

                                if (pendingDelete) {
                                    // wait for animation before removing
                                    LaunchedEffect(Unit) {
                                        delay(200)
                                        manageMembersViewModel.removeMember(
                                            member,
                                            group
                                        )
                                        manageMembersViewModel.removeMemberFromList(member)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .background(Color.White)
                                        .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing
                                        )
                                        )
                                ) {
                                    this@Column.AnimatedVisibility(
                                        visible = visible,
                                        exit = slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                    ) {
                                        AnimatedMemberItem(
                                            member.uid,
                                            selectedMember?.uid
                                        ) {
                                            MemberCard(
                                                member,
                                                "member",
                                                localImageLoaderValue,
                                                onClickInfoButton = {
                                                    onNavigateToUserInformationScreen(member)
                                                },
                                                onClickPromoteButton = {
                                                    selectedMember = member
                                                    manageMembersViewModel.promoteMember(
                                                        member,
                                                        group)
                                                },
                                                onClickRemoveButton = {
                                                    visible = false
                                                    pendingDelete = true
                                                },
                                                modifier = Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(adminPendingDeleteId) {
                        val targetId = adminPendingDeleteId
                        if (targetId != null && adminToDelete?.uid == targetId) {
                            delay(200)
                            adminToDelete?.let { admin ->
                                manageMembersViewModel.removeMember(
                                    admin,
                                    group
                                )
                                manageMembersViewModel.removeAdminFromList(admin)
                            }
                            adminPendingDeleteId = null
                        }
                    }

                    adminToDelete?.let { admin ->
                        UiUtils.ShowDiscardDialog(
                            "Remove Admin?",
                            "Are you sure you want to remove this admin from the group? This action cannot be undone.",
                            icon = Icons.Default.PersonRemove,
                            iconBackground = Color(0xFFD32F2F),
                            onDiscard = {
                                //If you are the last admin and there are members in group. Cannot remove you
                                if (adminList.size <= 1 && memberSet.isNotEmpty()) {
                                    //Cannot remove the last admin
                                    showToast("This is the last admin in the group. Cannot remove!")
                                } else {
                                    adminPendingDeleteId = admin.uid
                                    showAlertDialog.value = false
                                }
                            },
                            onCancel = {
                                adminToDelete = null
                            },
                            showAlertDialog
                        )
                    }
                }
                if(isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }
        fun getScreenName() : String {
            return "ManageMembersScreen"
        }

        @Composable
        fun MemberCard(
            user : UserInstance,
            role : String,
            localImageLoaderValue : ProvidedValue<*>,
            onClickInfoButton : () -> Unit,
            onClickPromoteButton : () -> Unit,
            onClickRemoveButton : () -> Unit,
            modifier: Modifier = Modifier
        ) {
            val isAdmin = (role == "admin")
            var expanded by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                label = ""
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if(isAdmin) adminCardColor else memberCardColor
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if(isAdmin) adminBorderColor else Color.LightGray
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(10.dp)
                    ) {
                        CompositionLocalProvider(
                            localImageLoaderValue
                        ) {
                            AutoSizeImage(
                                user.image.toStorageUrl(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = user.name,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp))
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                expanded = !expanded
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = if(isAdmin) MaterialTheme.colorScheme.primary else Color.LightGray
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 18.dp,
                                vertical = 6.dp
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if(isAdmin) MaterialTheme.colorScheme.primary else Color.White,
                                contentColor = if(isAdmin) Color.White else Color.Black
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp)
                        ) {
                            Text(
                                text = "Manage",
                                fontSize = 14.sp
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Down Arrow",
                                modifier = Modifier.rotate(rotation))
                        }
                    }

                    //-----------------Expandable content---------------------//
                    AnimatedVisibility(
                        visible = expanded,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { it / 2 }
                        ) + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Divider()
                            Spacer(Modifier.height(12.dp))
                            ActionRow(
                                isAdmin,
                                onClickInfoButton = {
                                    onClickInfoButton()
                                },
                                onClickRemoveButton = {
                                    onClickRemoveButton()
                                },
                                onClickPromoteButton = {
                                    onClickPromoteButton()
                                })
                        }
                    }
                }
            }
        }

        @Composable
        fun ActionRow(
            isAdmin : Boolean,
            onClickInfoButton : () -> Unit,
            onClickPromoteButton : () -> Unit,
            onClickRemoveButton : () -> Unit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.TAG_ACTION_BUTTON_OF_USER_IN_GROUP_ROW)
                    .semantics {
                        contentDescription = TestTag.TAG_ACTION_BUTTON_OF_USER_IN_GROUP_ROW
                    },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionItem("INFO",
                    Icons.Default.Info,
                    onClick = {
                        onClickInfoButton()
                    })
                ActionItem(
                    if(isAdmin) "DEMOTE" else "PROMOTE",
                    if(isAdmin) Icons.Outlined.ArrowDownward else Icons.Default.VerifiedUser,
                    onClick = {
                        onClickPromoteButton()
                    })
                ActionItem("REMOVE",
                    Icons.Default.Block,
                    false,
                    onClick = {
                        onClickRemoveButton()
                    })
            }
        }

        @Composable
        fun ActionItem(
            text: String,
            icon: ImageVector,
            isPositiveButton : Boolean = true,
            onClick: () -> Unit = {}
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if(isPositiveButton) positiveBackgroundButtonColor else Color.LightGray)
                        .testTag(TestTag.TAG_ACTION_BUTTON_OF_USER_IN_GROUP)
                        .semantics {
                            contentDescription = TestTag.TAG_ACTION_BUTTON_OF_USER_IN_GROUP
                        }
                        .clickable{
                            onClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = if(isPositiveButton) positiveTintColor else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        @Composable
        fun SectionHeader(header : String, subHeader: String, headerTextColor : Color) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = header,
                    color = headerTextColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = subHeader,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        @Composable
        fun AnimatedMemberItem(
            memberId: String,
            movingMemberId: String?,
            modifier: Modifier = Modifier,
            content: @Composable () -> Unit
        ) {
            val isMoving = movingMemberId?.let { it == memberId } ?: false

            val scale by animateFloatAsState(
                targetValue = if (isMoving) 1.02f else 1f,
                animationSpec = tween(200),
                label = "scale"
            )

            Box(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                content()
            }
        }

    }
}