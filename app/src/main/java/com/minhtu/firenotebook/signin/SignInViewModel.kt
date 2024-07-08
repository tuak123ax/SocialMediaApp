package com.minhtu.firenotebook.signin

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel : ViewModel() {
    private val _signInStatus = MutableStateFlow(SignInState())
    val signInStatus : StateFlow<SignInState> = _signInStatus.asStateFlow()


    fun updateUI(currentUser: FirebaseUser?, context : Context) {
        if(currentUser != null){

        } else {
            Toast.makeText(context, "Cannot Sign In", Toast.LENGTH_SHORT).show()
        }
    }
}