package com.minhtu.firesocialmedia.presentation.information

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.minhtu.firesocialmedia.domain.usecases.signin.SaveLoginActivityInfoUseCase
import com.minhtu.firesocialmedia.platform.logMessage
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InformationViewModel(
    private val saveSignUpInformationUseCase: SaveSignUpInformationUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val saveLoginActivityInfoUseCase : SaveLoginActivityInfoUseCase,
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

    var avatar by mutableStateOf(Constants.DEFAULT_DECADE_AVATAR_URL)
    fun updateAvatar(input:String){
        avatar = input
    }

    var username by mutableStateOf("")
    fun updateUsername(input : String){
        username = input
    }

    var phone by mutableStateOf("")
    fun updatePhone(input: String) {
        phone = input
    }

    private val currentUserId = mutableStateOf<String?>(null)
    fun finishSignUpStage(){
        viewModelScope.launch(ioDispatcher) {
            if(username.isEmpty()) {
                _addInformationStatus.value = false
            } else {
                currentUserId.value = getCurrentUserUidUseCase.invoke()
                val userInstance = UserInstance(
                    email = email,
                    image = avatar,
                    name = username,
                    status = "",
                    phone = phone,
                    token = getFCMTokenUseCase.invoke(),
                    uid = currentUserId.value ?: ""
                )
                val result = saveSignUpInformationUseCase.invoke(userInstance)
                _addInformationStatus.value = result
            }
        }
    }

    fun saveLoginActivityInfo() {
        viewModelScope.launch(ioDispatcher) {
            if(currentUserId.value != null) {
                logMessage("saveLoginActivityInfo", { "start save login activity info" })
                saveLoginActivityInfoUseCase.invoke(currentUserId.value!!)
            }
        }
    }
}