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
                           onNavigateToSignIn: () -> Unit){
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                modifier = modifier.padding(paddingValues)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 20.dp)
                )
//                val openChatAppIntent = getChatAppIntent(context)
//                val openChatAppLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){}
                val showDialog = remember { mutableStateOf(false) }
                UiUtils.Companion.ShowAlertDialogToLogout(onClickConfirm = {
                    homeViewModel.clearAccountInStorage()
                    homeViewModel.clearLocalData()
                }, onNavigateToSignIn, showDialog)
                //Fire chat button
                Button(
                    onClick = {
//                    if (openChatAppIntent != null) {
//                        openChatAppLauncher.launch(openChatAppIntent)
//                    } else {
//                        showToast("Can't find this app on your device!")
//                    }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Companion.White.copy(alpha = 0.95f),
                        contentColor = Color.Companion.Black
                    ),
                    modifier = Modifier.Companion
                        .padding(horizontal = 10.dp)
                        .border(
                            1.dp,
                            Color.Companion.Black,
                            androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                        .fillMaxWidth()
                        .testTag(TestTag.Companion.TAG_FIRECHAT_BUTTON)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_FIRECHAT_BUTTON
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                    ) {
                        CrossPlatformIcon(
                            icon = "fire_chat_icon",
                            backgroundColor = "#00FFFFFF",
                            contentDescription = "FireChat",
                            modifier = Modifier.Companion
                                .size(30.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "FireChat", color = Color.Companion.Black)
                    }
                }

                Spacer(modifier = Modifier.Companion.weight(1f))
                Button(
                    onClick = {
                        showDialog.value = true
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = Modifier.Companion
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .testTag(TestTag.Companion.TAG_BUTTON_LOGOUT)
                        .semantics {
                            contentDescription = TestTag.Companion.TAG_BUTTON_LOGOUT
                        },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = ButtonDefaults.buttonColors(Color.Companion.Gray)
                ) {
                    Text(text = "Logout")
                }
            }
        }

        fun getScreenName() : String {
            return "SettingsScreen"
        }
    }
}