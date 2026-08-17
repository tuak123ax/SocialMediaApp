package com.minhtu.firesocialmedia.presentation.information

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minhtu.firesocialmedia.storage.auth.SupabaseStorageProvider
import com.minhtu.firesocialmedia.constants.auth.TestTag
import com.minhtu.firesocialmedia.data.remote.service.auth.auth.AuthSessionService
import com.minhtu.firesocialmedia.data.remote.service.imagepicker.auth.ImagePicker
import com.minhtu.firesocialmedia.di.PlatformContext
import com.minhtu.firesocialmedia.platform.CommonBackHandler
import com.minhtu.firesocialmedia.platform.getImageBytesFromDrawable
import com.minhtu.firesocialmedia.platform.showToast
import com.minhtu.firesocialmedia.auth.presentation.loading.Loading
import com.minhtu.firesocialmedia.auth.presentation.loading.LoadingViewModel
import com.minhtu.firesocialmedia.utils.auth.TitleBarUtils
import com.minhtu.sharedmodule.ui.theme.avatarGrayBackground
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class Information {
    companion object {
        @Composable
        fun InformationScreen(
            platform: PlatformContext,
            imagePicker: ImagePicker,
            signUpEmail: String = "",
            signUpPassword: String = "",
            informationViewModel: InformationViewModel,
            onNavigateToHomeScreen: () -> Unit,
            authSessionService: AuthSessionService = koinInject()
        ) {
            val loadingViewModel: LoadingViewModel = koinViewModel()
            val isLoading by loadingViewModel.isLoading.collectAsState()
            imagePicker.RegisterLauncher { loadingViewModel.hideLoading() }

            val addInformationStatus = informationViewModel.addInformationStatus.collectAsState()
            LaunchedEffect(Unit) {
                if (signUpEmail.isNotEmpty()) {
                    informationViewModel.updateEmail(signUpEmail)
                    informationViewModel.updatePassword(signUpPassword)
                } else {
                    informationViewModel.updateEmail(authSessionService.getCurrentUserEmail().toString())
                }
            }
            LaunchedEffect(addInformationStatus.value) {
                if (addInformationStatus.value != null) {
                    loadingViewModel.hideLoading()
                    if (addInformationStatus.value!!) {
                        showToast("Sign up successfully!!!")
                        // Signup successfully, track this activity
                        informationViewModel.saveLoginActivityInfo()
                        onNavigateToHomeScreen()
                    } else {
                        showToast("Error happened!!!")
                    }
                    informationViewModel.resetAddInformationStatus()
                }
            }
            CommonBackHandler {
                showToast("Please finish sign up process!")
            }
            Box(modifier = Modifier.fillMaxSize()) {
                val avatarModifier = Modifier
                    .size(160.dp)
                    .background(avatarGrayBackground)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag(TestTag.TAG_SELECT_AVATAR)
                    .semantics {
                        contentDescription = TestTag.TAG_SELECT_AVATAR
                    }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))
                    TitleBarUtils.BackAndTitleAndMoreOptionsRow(
                        "Create Your Profile",
                        titleStyle = MaterialTheme.typography.titleLarge,
                        subTitle = "Customize how you appear to others",
                        showBackButton = false
                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    val imageBytes =
                        produceState<ByteArray?>(initialValue = null, informationViewModel.avatar) {
                            value = if (informationViewModel.avatar == SupabaseStorageProvider.DEFAULT_DECADE_AVATAR_URL) {
                                getImageBytesFromDrawable("decadeavatar")
                            } else {
                                imagePicker.loadImageBytes(informationViewModel.avatar)
                            }
                        }
                    if (imageBytes.value != null) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            imagePicker.ByteArrayImage(
                                imageBytes.value,
                                modifier = avatarModifier
                            )
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                onClick = {
                                    imagePicker.pickImage()
                                }
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    "Select avatar",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = "CHANGE AVATAR",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.padding(30.dp))
                    Text(
                        text = "Your name",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    OutlinedTextField(
                        value = informationViewModel.username,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                "Name"
                            )
                        },
                        onValueChange = {
                            informationViewModel.updateUsername(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag(TestTag.TAG_SELECT_NAME)
                            .semantics {
                                contentDescription = TestTag.TAG_SELECT_NAME
                            },
                        label = { Text(text = "Input Your Name") },
                        singleLine = true
                    )
                    Text(
                        text = "This name will be visible to your friends and in public interactions.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp)
                    )
                    Text(
                        text = "Phone number (optional)",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    OutlinedTextField(
                        value = informationViewModel.phone,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, "Phone")
                        },
                        onValueChange = { informationViewModel.updatePhone(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        label = { Text(text = "Input Your Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                loadingViewModel.showLoading()
                                informationViewModel.finishSignUpStage()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .testTag(TestTag.TAG_BUTTON_NEXT)
                                .semantics {
                                    contentDescription = TestTag.TAG_BUTTON_NEXT
                                }
                        ) {
                            Text(text = "Next Step →")
                        }
                    }
                    Text(
                        text = "You can change these details later in settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                }
                if (isLoading) {
                    Loading.LoadingScreen()
                }
            }
        }

        fun getScreenName(): String {
            return "InformationScreen"
        }
    }
}

