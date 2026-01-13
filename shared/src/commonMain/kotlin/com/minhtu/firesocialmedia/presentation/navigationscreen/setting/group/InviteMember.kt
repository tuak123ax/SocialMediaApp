package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.domain.entity.group.GroupInstance
import com.minhtu.firesocialmedia.presentation.search.Search
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.utils.UiUtils
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ShareAppRow

class InviteMember {
    companion object {
        @Composable
        fun InviteMemberScreen(
            group : GroupInstance,
            paddingValues: PaddingValues,
            inviteMemberViewModel: InviteMemberViewModel,
            searchViewModel: SearchViewModel,
            onNavigateBack : () -> Unit
        ) {
            var showBottomSheet by rememberSaveable { mutableStateOf(false) }
            Box(modifier = Modifier.padding(paddingValues)) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    UiUtils.BackAndTitleAndMoreOptionsRow(
                        title = "Invite Members",
                        onNavigateBack
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        thickness = 1.dp,
                        color = Color.LightGray
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
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    )
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
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.size(35.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = "Link",
                            tint = Color.Red
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Invite via link",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = groupId,
                            color = Color.Black,
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
                            contentColor = Color.White
                        )){
                        Icon(Icons.Filled.Share,
                            contentDescription = "Share Link",
                            tint = Color.White)
                        Text(
                            text = "Share Link",
                            color = Color.White,
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
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(if(copyStatus.value) Icons.Default.Check else Icons.Default.CopyAll,
                            contentDescription = "Copy",
                            tint = Color.Black)
                        Text(
                            text = if(copyStatus.value) "Copied" else "Copy",
                            color = Color.Black,
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
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Invite your friend to join the conversation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
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
                            color = Color.LightGray
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
                        .background(Color.White)
                ) {
                    Icon(
                        if(copyStatus.value) Icons.Default.Check else Icons.Default.Link,
                        "Share Link",
                        tint = Color.Red,
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
                        color = Color.Black,
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
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                    ){
                        Text(
                            text = if(copyStatus.value) "Copied" else "Copy",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }
    }
}