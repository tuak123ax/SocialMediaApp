package com.minhtu.firesocialmedia.presentation.information

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
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.constants.TestTag
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.ImagePicker
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.presentation.loading.Loading
import com.minhtu.firesocialmedia.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.presentation.signup.SignUpViewModel

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
            imagePicker.RegisterLauncher({loadingViewModel.hideLoading()})

            val addInformationStatus = informationViewModel.addInformationStatus.collectAsState()
            LaunchedEffect(Unit) {
                if (signUpViewModel.email.isNotEmpty()) {
                    informationViewModel.updateEmail(signUpViewModel.email)
                    informationViewModel.updatePassword(signUpViewModel.password)
                } else {
                    informationViewModel.updateEmail(platform.auth.getCurrentUserEmail().toString())
                }
            }
            LaunchedEffect(addInformationStatus.value) {
                loadingViewModel.hideLoading()
                if (addInformationStatus.value != null) {
                    if (addInformationStatus.value!!) {
                        showToast("Sign up successfully!!!")
                        onNavigateToHomeScreen()
                    } else {
                        showToast("Error happened!!!")
                    }
                }
            }
            Box(modifier = Modifier.Companion.fillMaxSize()) {
                val avatarModifier = Modifier.Companion
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Companion.Gray, CircleShape)
                    .clickable {
                        imagePicker.pickImage()
                    }
                    .testTag(TestTag.Companion.TAG_SELECT_AVATAR)
                    .semantics {
                        contentDescription = TestTag.Companion.TAG_SELECT_AVATAR
                    }
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text(
                        text = "Please select your avatar",
                        color = Color.Companion.White,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                    Spacer(modifier = Modifier.Companion.padding(20.dp))
                    val imageBytes =
                        produceState<ByteArray?>(initialValue = null, informationViewModel.avatar) {
                            if (informationViewModel.avatar == Constants.Companion.DEFAULT_AVATAR_URL) {
                                value = getImageBytesFromDrawable("unknownavatar")
                            } else {
                                value = imagePicker.loadImageBytes(informationViewModel.avatar)
                            }
                        }
                    if (imageBytes.value != null) {
                        imagePicker.ByteArrayImage(
                            imageBytes.value,
                            modifier = avatarModifier
                        )
                        loadingViewModel.hideLoading()
                    }
                    Spacer(modifier = Modifier.Companion.padding(20.dp))
                    Text(
                        text = "And input your name below",
                        color = Color.Companion.White,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Companion.Center,
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.Companion.padding(20.dp))
                    OutlinedTextField(
                        value = informationViewModel.username,
                        shape = RoundedCornerShape(30.dp),
                        textStyle = TextStyle(color = Color.Companion.White),
                        onValueChange = {
                            informationViewModel.updateUsername(it)
                        }, modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(20.dp)
                            .testTag(TestTag.Companion.TAG_SELECT_NAME)
                            .semantics {
                                contentDescription = TestTag.Companion.TAG_SELECT_NAME
                            },
                        label = { Text(text = "Name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.Companion.padding(20.dp))
                    Row(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                loadingViewModel.showLoading()
                                informationViewModel.finishSignUpStage()
                            },
                            modifier = Modifier.Companion.testTag(TestTag.Companion.TAG_BUTTON_NEXT)
                                .semantics {
                                    contentDescription = TestTag.Companion.TAG_BUTTON_NEXT
                                }) {
                            Text(text = "Next")
                        }
                    }
                }
                if (isLoading) {
                    Loading.Companion.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String{
            return "InformationScreen"
        }
    }
}