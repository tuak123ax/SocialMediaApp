package com.minhtu.firesocialmedia.presentation.navigationscreen.setting.group

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.platform.showToast

class Group {
    companion object{
        @Composable
        fun GroupScreen(
            currentUser : UserInstance,
            onNavigateToCreateGroupScreen: () -> Unit,
            onNavigateToExploreGroupScreen : () -> Unit,
            onNavigateToSelectGroupScreen : () -> Unit
        ) {
            val firstTimeUseGroup = currentUser.groups.isEmpty()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if(firstTimeUseGroup) {
                    Text(
                        text = "Welcome to Group",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    CrossPlatformIcon(
                        icon = "group_background",
                        backgroundColor = "#00FFFFFF",
                        contentDescription = "group background",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .padding(end = 5.dp)
                    )
                } else {
                    Text(
                        text = "Welcome back",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(50.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Click here to access your groups",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        //Create group button
                        Button(
                            onClick = {
                                onNavigateToSelectGroupScreen()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.95f),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .border(
                                    1.dp,
                                    Color.Black,
                                    RoundedCornerShape(10.dp)
                                )
                                .fillMaxWidth()
                                .testTag(TestTag.TAG_SELECT_GROUP_BUTTON)
                                .semantics {
                                    contentDescription = TestTag.TAG_SELECT_GROUP_BUTTON
                                }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                CrossPlatformIcon(
                                    icon = "select_group",
                                    backgroundColor = "#00FFFFFF",
                                    contentDescription = "select_group",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(end = 5.dp)
                                )
                                Text(text = "Select Your Group", color = Color.Black)
                                Spacer(Modifier.weight(1f))
                                CrossPlatformIcon(
                                    icon = "right",
                                    backgroundColor = "#00FFFFFF",
                                    contentDescription = "right",
                                    modifier = Modifier
                                        .size(40.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(50.dp))
                    Text(
                        text = "Or explore other groups",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //Create group button
                    Button(
                        onClick = {
                            if(currentUser.groups.size < 50) {
                                onNavigateToCreateGroupScreen()
                            } else {
                                showToast("You only can join 50 groups at the same time!")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.95f),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .border(
                                1.dp,
                                Color.Black,
                                RoundedCornerShape(10.dp)
                            )
                            .fillMaxWidth()
                            .testTag(TestTag.TAG_CREATE_GROUP_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_CREATE_GROUP_BUTTON
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            CrossPlatformIcon(
                                icon = "create_group",
                                backgroundColor = "#00FFFFFF",
                                contentDescription = "create_group",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Create Group", color = Color.Black)
                            Spacer(Modifier.weight(1f))
                            CrossPlatformIcon(
                                icon = "right",
                                backgroundColor = "#00FFFFFF",
                                contentDescription = "right",
                                modifier = Modifier
                                    .size(40.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    //Join group button
                    Button(
                        onClick = {
                            if(currentUser.groups.size < 50) {
                                onNavigateToExploreGroupScreen()
                            } else {
                                showToast("You only can join 50 groups at the same time!")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.95f),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .border(
                                1.dp,
                                Color.Black,
                                RoundedCornerShape(10.dp)
                            )
                            .fillMaxWidth()
                            .testTag(TestTag.TAG_FIND_GROUP_BUTTON)
                            .semantics {
                                contentDescription = TestTag.TAG_FIND_GROUP_BUTTON
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            CrossPlatformIcon(
                                icon = "explore_group",
                                backgroundColor = "#00FFFFFF",
                                contentDescription = "explore_group",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(text = "Explore Group", color = Color.Black)
                            Spacer(Modifier.weight(1f))
                            CrossPlatformIcon(
                                icon = "right",
                                backgroundColor = "#00FFFFFF",
                                contentDescription = "right",
                                modifier = Modifier
                                    .size(40.dp)
                            )
                        }
                    }
                }
            }
        }

        fun getScreenName() : String {
            return "GroupScreen"
        }
    }
}