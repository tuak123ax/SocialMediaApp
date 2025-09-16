package com.minhtu.firesocialmedia.presentation.information

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.domain.entity.user.UserInstance
import com.minhtu.firesocialmedia.domain.usecases.common.GetCurrentUserUidUseCase
import com.minhtu.firesocialmedia.domain.usecases.common.GetFCMTokenUseCase
import com.minhtu.firesocialmedia.domain.usecases.information.SaveSignUpInformationUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class InformationViewModel(
    private val saveSignUpInformationUseCase: SaveSignUpInformationUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
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

    fun finishSignUpStage(){
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if(username.isEmpty()) {
                    _addInformationStatus.value = false
                } else {
                    val uid = getCurrentUserUidUseCase.invoke()
                    val userInstance = UserInstance(email, avatar,username,"",
                        getFCMTokenUseCase.invoke(),uid!!, HashMap())
                    val result = saveSignUpInformationUseCase.invoke(userInstance)
                    _addInformationStatus.value = result
                }
            }
        }
    }
}