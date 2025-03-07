package com.minhtu.firesocialmedia.forgotpassword

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.constants.Constants

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    private var _emailExisted : MutableLiveData<Pair<Boolean, String>> = MutableLiveData()
    var emailExisted = _emailExisted
    fun checkIfEmailExists() {
        if(email.isNotEmpty()) {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods.isNullOrEmpty()) {
                            Log.d("FirebaseAuth", "Email does NOT exist.")
                            _emailExisted.postValue(Pair(false, Constants.EMAIL_NOT_EXISTED))
                        } else {
                            Log.d("FirebaseAuth", "Email exists.")
                            _emailExisted.postValue(Pair(true, Constants.EMAIL_EXISTED))
                        }
                    } else {
                        Log.e("FirebaseAuth", "Error: ${task.exception?.message}")
                        _emailExisted.postValue(Pair(false, Constants.EMAIL_SERVER_ERROR))
                    }
                }
        } else {
            _emailExisted.postValue(Pair(false, Constants.EMAIL_EMPTY))
        }
    }

    private var _emailSent : MutableLiveData<Boolean> = MutableLiveData()
    var emailSent = _emailSent
    fun sendEmailResetPassword() {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Log.d("FirebaseAuth", "Email sent.")
                    _emailSent.postValue(true)
                } else {
                    Log.e("FirebaseAuth", "Error: ${task.exception?.message}")
                    _emailSent.postValue(false)
                }
            }
    }
    fun resetEmailResetPassword(){
        _emailExisted = MutableLiveData()
        emailExisted = _emailExisted
        _emailSent = MutableLiveData<Boolean>()
        emailSent = _emailSent
    }
}