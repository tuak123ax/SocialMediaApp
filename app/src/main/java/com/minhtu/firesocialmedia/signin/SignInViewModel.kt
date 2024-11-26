package com.minhtu.firesocialmedia.signin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.minhtu.firesocialmedia.constants.Constants

class SignInViewModel : ViewModel() {
    private val _signInStatus = MutableLiveData<SignInState>()
    val signInState = _signInStatus
    var email by mutableStateOf("")
    fun updateEmail(input : String){
        email = input
    }

    var password by mutableStateOf("")
    fun updatePassword(input : String){
        password =  input
    }

    fun signIn(context: Context){
        if(email == "" || password == "")
        {
            signInState.postValue(SignInState(false, Constants.DATA_EMPTY))
            Log.e("SignInViewModel","signIn: DATA_EMPTY")
        }else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    task->
                    if(task.isSuccessful){
                        saveAccount(context, email, password)
                        signInState.postValue(SignInState(true, ""))
                    } else{
                        signInState.postValue(SignInState(false, Constants.LOGIN_ERROR))
                        Log.e("SignInViewModel","signIn: LOGIN_ERROR")
                    }
                }
        }
    }

    private fun saveAccount(context: Context, email: String, password: String){
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("local_data", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("password", password).apply()
    }

//    fun updateUI(currentUser: FirebaseUser?, context : Context) {
//        if(currentUser != null){
//
//        } else {
//            Toast.makeText(context, "Cannot Sign In", Toast.LENGTH_SHORT).show()
//        }
//    }
}