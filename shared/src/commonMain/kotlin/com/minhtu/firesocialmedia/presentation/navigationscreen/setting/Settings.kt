package com.minhtu.firesocialmedia.presentation.navigationscreen.setting

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.platform.CrossPlatformIcon
import com.minhtu.firesocialmedia.presentation.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class Settings {
    companion object{
        @Composable
        fun SettingsScreen(modifier: Modifier,
                           paddingValues: PaddingValues,
                           homeViewModel: HomeViewModel,
                           onNavigateToSignIn: () -> Unit,
                           onNavigateToGroupScreen: () -> Unit){
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
                val showDialog = remember { mutableStateOf(false) }
                UiUtils.ShowAlertDialogToLogout(
                    onClickConfirm = {
                    homeViewModel.clearAccountInStorage()
                    homeViewModel.clearLocalData()
                },
                    onNavigateToSignIn,
                    showDialog)
                //Group button
                Button(
                    onClick = {
                        onNavigateToGroupScreen()
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
                            androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                        .fillMaxWidth()
                        .testTag(TestTag.TAG_GROUP_BUTTON)
                        .semantics {
                            contentDescription = TestTag.TAG_GROUP_BUTTON
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        CrossPlatformIcon(
                            icon = "group",
                            backgroundColor = "#00FFFFFF",
                            contentDescription = "group",
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "Group", color = Color.Black)
                        Spacer(Modifier.weight(1f))
                        CrossPlatformIcon(
                            icon = "right",
                            backgroundColor = "#00FFFFFF",
                            contentDescription = "right",
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        showDialog.value = true
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .testTag(TestTag.TAG_BUTTON_LOGOUT)
                        .semantics {
                            contentDescription = TestTag.TAG_BUTTON_LOGOUT
                        },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = ButtonDefaults.buttonColors(Color.Gray)
                ) {
                    Text(text = "Logout")
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        fun getScreenName() : String {
            return "SettingsScreen"
        }
    }
}