package com.minhtu.firesocialmedia.information

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minhtu.firesocialmedia.ImagePicker
import com.minhtu.firesocialmedia.PlatformContext
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.logMessage
import com.minhtu.firesocialmedia.showToast
import com.minhtu.firesocialmedia.signup.SignUpViewModel

class Information {
    companion object{
        @Composable
        fun InformationScreen(modifier: Modifier,
                              platform: PlatformContext,
                              imagePicker: ImagePicker,
                              signUpViewModel: SignUpViewModel,
                              informationViewModel: InformationViewModel,
                              loadingViewModel: LoadingViewModel,
                              onNavigateToHomeScreen: () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            imagePicker.RegisterLauncher { loadingViewModel.hideLoading() }

            val addInformationStatus = informationViewModel.addInformationStatus.collectAsState()
            LaunchedEffect(Unit) {
                if(signUpViewModel.email.isNotEmpty()){
                    informationViewModel.updateEmail(signUpViewModel.email)
                    informationViewModel.updatePassword(signUpViewModel.password)
                } else {
                    informationViewModel.updateEmail(platform.auth.getCurrentUserEmail().toString())
                }
            }
            LaunchedEffect(addInformationStatus.value) {
                loadingViewModel.hideLoading()
                if(addInformationStatus.value != null) {
                    if(addInformationStatus.value!!) {
                        showToast("Sign up successfully!!!")
                        onNavigateToHomeScreen()
                    } else {
                        showToast("Error happened!!!")
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                val avatarModifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
                    .clickable {
                        imagePicker.pickImage()
                    }
                    .testTag(TestTag.TAG_SELECT_AVATAR)
                    .semantics{
                        contentDescription = TestTag.TAG_SELECT_AVATAR
                    }
                Column(modifier = modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "Please select your avatar",
                        color = Color.White,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp))
                    Spacer(modifier = Modifier.padding(20.dp))
                    var imageBytes = produceState<ByteArray?>(initialValue = null, informationViewModel.avatar) {
                        if(informationViewModel.avatar == Constants.DEFAULT_AVATAR_URL) {
                            value = getImageBytesFromDrawable("unknownavatar")
                        } else {
                            value = imagePicker.loadImageBytes(informationViewModel.avatar)
                        }
                    }
                    if(imageBytes.value != null) {
                        imagePicker.ByteArrayImage(
                            imageBytes.value,
                            modifier = avatarModifier
                        )
                    }
                    Spacer(modifier = Modifier.padding(20.dp))
                    Text(text = "And input your name below",
                        color = Color.White,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.padding(20.dp))
                    OutlinedTextField(
                        value = informationViewModel.username,
                        shape = RoundedCornerShape(30.dp),
                        textStyle = TextStyle(color = Color.White),
                        onValueChange = {
                            informationViewModel.updateUsername(it)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.TAG_SELECT_NAME)
                            .semantics{
                                contentDescription = TestTag.TAG_SELECT_NAME
                            },
                        label = { Text(text = "Name")},
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                        Button(onClick = {
                            loadingViewModel.showLoading()
                            informationViewModel.finishSignUpStage(platform)},
                            modifier = Modifier.testTag(TestTag.TAG_BUTTON_NEXT)
                                .semantics{
                                    contentDescription = TestTag.TAG_BUTTON_NEXT
                                }){
                            Text(text = "Next")
                        }
                    }
                }
                if(isLoading){
                    Loading.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String{
            return "InformationScreen"
        }
    }
}