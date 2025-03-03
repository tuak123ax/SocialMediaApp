package com.minhtu.firesocialmedia.signin

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.crypto.CryptoHelper
import com.minhtu.firesocialmedia.instance.UserInstance
import com.minhtu.firesocialmedia.signin.SignInState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        viewModelScope.launch {
            if(email == "" || password == "")
            {
                signInState.postValue(SignInState(false, Constants.DATA_EMPTY))
                Log.e("SignInViewModel","signIn: DATA_EMPTY")
            }else{
                withContext(Dispatchers.IO){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener{
                                task->
                            if(task.isSuccessful){
                                CryptoHelper.saveAccount(context, email, password)
                                signInState.postValue(SignInState(true, ""))
                            } else{
                                signInState.postValue(SignInState(false, Constants.LOGIN_ERROR))
                                Log.e("SignInViewModel","signIn: LOGIN_ERROR")
                            }
                        }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context, resultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                setupGoogleSignIn(context, resultLauncher)
            }
        }
    }

    private fun setupGoogleSignIn(context: Context, resultLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>){
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("744458948813-qktjfopd2cr9b1a87pbr3981ujllb3mt.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()
        val googleSignInClient = Identity.getSignInClient(context)
        googleSignInClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
            try {
                // Launch the One Tap UI
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                resultLauncher.launch(intentSenderRequest)
            } catch (e: IntentSender.SendIntentException) {
                Log.e("OneTapSignIn", "Error launching intent: ${e.localizedMessage}")
            }
        }
            .addOnFailureListener { exception ->
                Log.e("OneTapSignIn", "Sign-in failed: ${exception.localizedMessage}")
            }
    }

    fun handleSignInResult(credential : SignInCredential, activity: Any) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        Firebase.auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(activity as Activity) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Signin", "signInWithCredential:success")
                                    val user = Firebase.auth.currentUser
                                    emailExistedInDatabase(user!!.email)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Signin", "signInWithCredential:failure", task.exception)
                                    signInState.postValue(SignInState(false, Constants.LOGIN_ERROR))
                                    Log.e("SignInViewModel","signIn: LOGIN_ERROR")
                                }
                            }
                    }
                    else -> {
                        // Shouldn't happen.
                        Log.d("Signin", "No ID token!")
                    }
                }
            }
        }
    }

    private fun emailExistedInDatabase(email: String?) {
        if(email != null) {
            val database = FirebaseDatabase.getInstance()
            val databaseReference: DatabaseReference = database.getReference().child("users")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var existed = false
                    for (dataSnapshot in snapshot.getChildren()) {
                        val user: UserInstance? = dataSnapshot.getValue(UserInstance::class.java)
                        if (user != null) {
                            if(user.email == email){
                                signInState.postValue(SignInState(true, Constants.ACCOUNT_EXISTED))
                                Log.e("SignInViewModel","signIn: ACCOUNT_EXISTED")
                                existed = true
                            }
                        }
                    }
                    if(!existed) {
                        signInState.postValue(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                        Log.e("SignInViewModel","signIn: ACCOUNT_NOT_EXISTED")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    signInState.postValue(SignInState(false, Constants.LOGIN_ERROR))
                    Log.e("SignInViewModel","signIn: LOGIN_ERROR")
                }
            })
        }
    }

    fun checkAccountInLocalStorage(context: Context){
        viewModelScope.launch{
            withContext(Dispatchers.IO) {
                val secureSharedPreferences: SharedPreferences = CryptoHelper.getEncryptedSharedPreferences(context)
                val email = secureSharedPreferences.getString(Constants.KEY_EMAIL, "")
                val password = secureSharedPreferences.getString(Constants.KEY_PASSWORD, "")
                if(email != null && password != null) {
                    if(email.isNotEmpty() && password.isNotEmpty()){
                        updateEmail(email)
                        updatePassword(password)
                        signIn(context)
                    }
                }
            }
        }
    }
}