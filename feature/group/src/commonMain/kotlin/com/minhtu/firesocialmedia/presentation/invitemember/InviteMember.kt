package com.minhtu.firesocialmedia.presentation.invitemember

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.group.TestTag
import com.minhtu.firesocialmedia.constants.group.DataConstant
import com.minhtu.firesocialmedia.group.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.storage.group.toStorageUrl
import com.minhtu.firesocialmedia.utils.group.TitleBarUtils
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.GroupSearchBar
import com.minhtu.firesocialmedia.group.utils.UiUtils.Companion.ShareAppRow
import com.seiko.imageloader.ui.AutoSizeImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.compose.viewmodel.koinViewModel

class InviteMember {
    companion object {
        @Composable
        fun InviteMemberScreen(
            groupId : String,
            currentUser : UserInstance,
            paddingValues: PaddingValues,
            localImageLoaderValue : ProvidedValue<*>,
            inviteMemberViewModel: InviteMemberViewModel = koinViewModel(),
            onNavigateBack : () -> Unit
        ) {
            CommonBackHandler {
                onNavigateBack()
            }
            var searchQuery by remember { mutableStateOf("") }
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            val fetchedGroup by inviteMemberViewModel.fetchGroupInfoState.collectAsState()
            LaunchedEffect(groupId) {
                inviteMemberViewModel.fetchGroupInfo(groupId)
            }
            if (fetchedGroup == null) {
                return
            }
            val group = fetchedGroup!!
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TitleBarUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Invite Members",
                        trailingIcon = "more_horiz",
                        navigateBack = onNavigateBack
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    GroupSearchBar(
                        query = searchQuery,
                        onQueryChange = { query -> searchQuery = query },
                        modifier = Modifier.height(80.dp).padding(vertical = 10.dp)
                            .testTag(TestTag.TAG_SEARCH_BAR)
                            .semantics {
                                contentDescription = TestTag.TAG_SEARCH_BAR
                            }
                    )
                    InviteCard(
                        group.id,
                        onClickCopyLink = {
                            inviteMemberViewModel.copyLink(
                                "${DataConstant.DEEP_LINK}/groups/${group.id}"
                            )
                        },
                        onClickShareLink = {
                            showBottomSheet = true
                        })
                    Text(
                        text = "ALL CONTACTS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    if(currentUser.friends.isNotEmpty()) {
                        var memberList by remember { mutableStateOf<List<UserInstance>>(emptyList()) }
                        // Run filtering when friend list or search query changes
                        LaunchedEffect(searchQuery) {
                            memberList = coroutineScope {
                                currentUser.friends.map { userId ->
                                    async {
                                        inviteMemberViewModel.findUserById(userId)
                                            ?.takeIf { it.name.contains(searchQuery, ignoreCase = true) }
                                    }
                                }.awaitAll().filterNotNull()
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                        ) {
                            items(memberList) { friend ->
                                InviteFriendCard(
                                    friend,
                                    localImageLoaderValue,
                                    onInviteFriend = { invitedFriend ->
                                        inviteMemberViewModel.inviteFriendToGroup(
                                            currentUser,
                                            invitedFriend,
                                            group)
                                    }
                                    )
                            }
                        }
                    }
                }
                if(showBottomSheet) {
                    ShareGroupBottomSheet(
                        deepLink = "${DataConstant.DEEP_LINK}/groups/${group.id}",
                        onDismiss = {
                            showBottomSheet = false
                        },
                        onCopyLink = {
                            inviteMemberViewModel.copyLink(
                                "${DataConstant.DEEP_LINK}/groups/${group.id}"
                            )
                        }
                    )
                }
            }
        }

        fun getScreenName() : String {
            return "InviteMemberScreen"
        }

        @Composable
        fun InviteCard(
            groupId : String,
            onClickCopyLink : () -> Unit,
            onClickShareLink : () -> Unit) {
            val copyStatus = remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 10.dp)
                ) {
                    Button(
                        onClick = {
                            //Click link button
                            copyStatus.value = true
                            onClickCopyLink()
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
                        Icon(
                            Icons.Default.Link,
                            contentDescription = "Link",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Invite via link",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = groupId,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Button(
                        onClick = {
                            //Share link
                            onClickShareLink()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.surface
                        )){
                        Icon(Icons.Filled.Share,
                            contentDescription = "Share Link",
                            tint = MaterialTheme.colorScheme.surface)
                        Text(
                            text = "Share Link",
                            color = MaterialTheme.colorScheme.surface,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = {
                            //Click copy button
                            copyStatus.value = true
                            onClickCopyLink()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(if(copyStatus.value) Icons.Default.Check else Icons.Default.CopyAll,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if(copyStatus.value) "Copied" else "Copy",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ShareGroupBottomSheet(
            deepLink : String,
            onDismiss: () -> Unit,
            onCopyLink : () -> Unit
        ) {
            ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = rememberModalBottomSheetState(),
            ) {
                // Sheet Content
                Column(Modifier.padding(10.dp)) {
                    Text(
                        "Share Group Link",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Invite your friend to join the conversation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    CopyGroupLinkCard(
                        deepLink,
                        onCopyLink
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Share via")
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    //Show all apps that can handle the shared link
                    ShareAppRow(deepLink)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        @Composable
        fun CopyGroupLinkCard(groupLink : String,
                              onCopyLink : () -> Unit) {
            val copyStatus = remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        if(copyStatus.value) Icons.Default.Check else Icons.Default.Link,
                        "Share Link",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable {
                                //Copy link
                                onCopyLink()
                                copyStatus.value = true
                            }
                    )

                    Text(
                        groupLink,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                //Copy link
                                onCopyLink()
                                copyStatus.value = true
                            }
                    )

                    Button(
                        onClick = {
                            //Copy link
                            onCopyLink()
                            copyStatus.value = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                    ){
                        Text(
                            text = if(copyStatus.value) "Copied" else "Copy",
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }

        @Composable
        fun InviteFriendCard(
            user : UserInstance,
            localImageLoaderValue : ProvidedValue<*>,
            onInviteFriend : (UserInstance) -> Unit
        ) {
            var inviteStatus by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Spacer(Modifier.width(20.dp))
                CompositionLocalProvider(
                    localImageLoaderValue
                ) {
                    AutoSizeImage(
                        user.image.toStorageUrl(),
                        contentDescription = "Friend Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .blur(if(!inviteStatus) 0.dp else 1.dp)
                    )
                }
                Text(
                    text = user.name,
                    color = if(!inviteStatus) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp))
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        inviteStatus = true
                        onInviteFriend(user)
                              },
                    enabled = !inviteStatus,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = if (!inviteStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 18.dp,
                        vertical = 6.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (!inviteStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (!inviteStatus) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outline,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp)
                ) {
                    Text(
                        text = if (!inviteStatus) "Invite" else "Sent ✓",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}