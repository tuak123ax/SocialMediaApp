package com.minhtu.firesocialmedia.signup

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.constants.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(): ViewModel() {
    private val _signUpStatus = MutableLiveData<SignUpState>()
    val signUpStatus = _signUpStatus

    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    var password by mutableStateOf("")
    fun updatePassword(input : String){
        password =  input
    }

    var confirmPassword by mutableStateOf("")
    fun updateConfirmPassword(input : String){
        confirmPassword =  input
    }

    fun signUp(){
        viewModelScope.launch {
            if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
            {
                _signUpStatus.postValue(SignUpState(false, Constants.DATA_EMPTY))
                Log.e("SignUpViewModel","signUp: DATA_EMPTY")
            } else{
                if(password != confirmPassword){
                    _signUpStatus.postValue(SignUpState(false, Constants.PASSWORD_MISMATCH))
                    Log.e("SignUpViewModel","signUp: PASSWORD_MISMATCH")
                } else{
                    if(password.length < 6){
                        _signUpStatus.postValue(SignUpState(false, Constants.PASSWORD_SHORT))
                        Log.e("SignUpViewModel","signUp: PASSWORD_SHORT")
                    } else{
                        withContext(Dispatchers.IO) {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful){
                                        _signUpStatus.postValue(SignUpState(true, ""))
                                        Log.e("SignUpViewModel","signUp: success")
                                    } else{
                                        _signUpStatus.postValue(SignUpState(false, Constants.SIGNUP_FAIL))
                                        Log.e("SignUpViewModel","signUp: SIGNUP_FAIL")
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}