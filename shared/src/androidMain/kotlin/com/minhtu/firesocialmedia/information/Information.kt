package com.minhtu.firesocialmedia.information

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.minhtu.firesocialmedia.loading.Loading
import com.minhtu.firesocialmedia.loading.LoadingViewModel
import com.minhtu.firesocialmedia.signup.SignUpViewModel

class Information {
    companion object{
        @Composable
        fun InformationScreen(modifier: Modifier,
                              signUpViewModel: SignUpViewModel,
                              informationViewModel: InformationViewModel = viewModel(),
                              loadingViewModel: LoadingViewModel = viewModel(),
                              onNavigateToHomeScreen: () -> Unit){
            val isLoading by loadingViewModel.isLoading.collectAsState()
            val galleryIntent = intentNavigateToGallery()
            val getAvatarFromGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
                    result ->
                if(result.resultCode == Activity.RESULT_OK){
                    Log.e("getAvatarFromGalleryLauncher", "RESULT_OK")
                    val image_url = result.data?.data
                    if(image_url != null){
                        Log.e("getAvatarFromGalleryLauncher", "update avatar: $image_url")
                        informationViewModel.updateAvatar(image_url.toString())
                    }
                }
            }

            val context = LocalContext.current
            val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
            LaunchedEffect(lifecycleOwner.value) {
                if(signUpViewModel.email.isNotEmpty()){
                    informationViewModel.updateEmail(signUpViewModel.email)
                    informationViewModel.updatePassword(signUpViewModel.password)
                } else {
                    informationViewModel.updateEmail(Firebase.auth.currentUser!!.email.toString())
                }
                informationViewModel.addInformationStatus.observe(lifecycleOwner.value) {addInformationStatus ->
                    loadingViewModel.hideLoading()
                    if(addInformationStatus) {
                        Log.e("Signup", informationViewModel.email)
                        Toast.makeText(context, "Sign up successfully!!!", Toast.LENGTH_SHORT).show()
                        onNavigateToHomeScreen()
                    } else {
                        Toast.makeText(context, "Error!!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "Please select your avatar",
                        color = Color.White,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp))
                    Spacer(modifier = Modifier.padding(20.dp))
                    AsyncImage(model = informationViewModel.avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable { getAvatarFromGalleryLauncher.launch(galleryIntent) })
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
                            .padding(20.dp),
                        label = { Text(text = "Name")},
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                        Button(onClick = {
                            loadingViewModel.showLoading()
                            informationViewModel.finishSignUpStage(context)}){
                            Text(text = "Next")
                        }
                    }
                }
                if(isLoading){
                    Loading.LoadingScreen()
                }
            }
        }
        private fun intentNavigateToGallery(): Intent {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            return intent
        }

        fun getScreenName(): String{
            return "InformationScreen"
        }
    }
}