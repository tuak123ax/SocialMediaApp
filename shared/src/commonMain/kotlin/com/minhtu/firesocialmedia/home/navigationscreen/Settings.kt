package com.minhtu.firesocialmedia.home.navigationscreen

import androidx.compose.foundation.Image
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
import com.minhtu.firesocialmedia.CrossPlatformIcon
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.getIconPainter
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils.Companion.ShowAlertDialogToLogout

class Settings {
    companion object{
        @Composable
        fun SettingsScreen(modifier: Modifier,
                           platform : PlatformContext,
                           paddingValues: PaddingValues,
                           homeViewModel: HomeViewModel,
                           onNavigateToSignIn: () -> Unit){
            Column(verticalArrangement = Arrangement.Top,horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(paddingValues)) {
                Text(text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
//                val openChatAppIntent = getChatAppIntent(context)
//                val openChatAppLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){}
                val showDialog = remember { mutableStateOf(false) }
                ShowAlertDialogToLogout(onClickConfirm = {
                    homeViewModel.clearAccountInStorage(platform)
                }, onNavigateToSignIn, showDialog)
                //Fire chat button
                Button(onClick = {
//                    if (openChatAppIntent != null) {
//                        openChatAppLauncher.launch(openChatAppIntent)
//                    } else {
//                        showToast("Can't find this app on your device!")
//                    }
                },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.95f),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .testTag(TestTag.TAG_FIRECHAT_BUTTON)
                        .semantics{
                            contentDescription = TestTag.TAG_FIRECHAT_BUTTON
                        }
                ) {
                    Row(horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()){
                        CrossPlatformIcon(
                            icon = "fire_chat_icon",
                            color = "#00FFFFFF",
                            contentDescription = "FireChat",
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 5.dp)
                        )
                        Text(text = "FireChat", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    showDialog.value = true
                },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .testTag(TestTag.TAG_BUTTON_LOGOUT)
                        .semantics{
                            contentDescription = TestTag.TAG_BUTTON_LOGOUT
                        },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp),
                    colors = ButtonDefaults.buttonColors(Color.Gray)
                ){
                    Text(text = "Logout")
                }
            }
        }

        fun getScreenName() : String {
            return "SettingsScreen"
        }

//        private fun getChatAppIntent(context: Context): Intent? {
//            val packageName = "com.example.firechat"
//            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
//            if(intent != null) {
//                return intent
//            }
//            return null
//        }
    }
}