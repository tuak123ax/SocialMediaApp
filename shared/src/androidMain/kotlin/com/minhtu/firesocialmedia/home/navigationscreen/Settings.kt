package com.minhtu.firesocialmedia.home.navigationscreen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.R
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.home.HomeViewModel
import com.minhtu.firesocialmedia.utils.UiUtils

class Settings {
    companion object{
        @Composable
        fun SettingsScreen(modifier: Modifier,
                           paddingValues: PaddingValues,
                           homeViewModel: HomeViewModel,
                           onNavigateToSignIn: () -> Unit){
            Column(verticalArrangement = Arrangement.Top,horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(paddingValues)) {
                val context = LocalContext.current
                Text(text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
                val openChatAppIntent = getChatAppIntent(context)
                val openChatAppLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){}
                val showDialog = remember { mutableStateOf(false) }
                UiUtils.ShowAlertDialogToLogout(context, homeViewModel, onNavigateToSignIn, showDialog)
                //Fire chat button
                Button(onClick = {
                    if (openChatAppIntent != null) {
                        openChatAppLauncher.launch(openChatAppIntent)
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Can't find this app on your device!",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
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
                        Image(
                            painter = painterResource(id = R.drawable.fire_chat_icon),
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

        private fun getChatAppIntent(context: Context): Intent? {
            val packageName = "com.example.firechat"
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if(intent != null) {
                return intent
            }
            return null
        }
    }
}