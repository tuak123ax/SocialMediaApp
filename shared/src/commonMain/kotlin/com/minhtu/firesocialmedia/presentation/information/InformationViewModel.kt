package com.minhtu.firesocialmedia.presentation.information

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.di.PlatformContext
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class InformationViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _addInformationStatus = MutableStateFlow<Boolean?>(null)
    val addInformationStatus = _addInformationStatus.asStateFlow()

    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    var password by mutableStateOf("")
    fun updatePassword(input : String){
        password =  input
    }

    var avatar by mutableStateOf(Constants.DEFAULT_AVATAR_URL)
    fun updateAvatar(input:String){
        avatar = input
    }

    var username by mutableStateOf("")
    fun updateUsername(input : String){
        username = input
    }

    fun finishSignUpStage(platform: PlatformContext){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(username.isEmpty()) {
                    _addInformationStatus.value = false
                } else {
                    val uid = platform.auth.getCurrentUserUid()
                    val userInstance = UserInstance(email, avatar,username,"",
                        platform.crypto.getFCMToken(),uid!!, HashMap())
                    platform.database.saveSignUpInformation(userInstance,
                        object : Utils.Companion.SaveSignUpInformationCallBack{
                            override fun onSuccess() {
                                _addInformationStatus.value = true
                            }

                            override fun onFailure() {
                                _addInformationStatus.value = false
                            }
                        })
                }
            }
        }
    }
}