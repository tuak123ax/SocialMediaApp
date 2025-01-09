package com.minhtu.firesocialmedia.information

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.UserInstance

class InformationViewModel : ViewModel() {
    private var userInstance : UserInstance? = null

    private val _addInformationStatus = MutableLiveData<Boolean>()
    val addInformationStatus = _addInformationStatus

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

    fun finishSignUpStage(context: Context){
        val uid = FirebaseAuth.getInstance().uid
        val storageReference = FirebaseStorage.getInstance().getReference()
            .child("avatar").child(uid!!)
        val databaseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(uid)
        userInstance = UserInstance(email, avatar,username,"",
            context.getSharedPreferences("local_data", Context.MODE_PRIVATE).getString(Constants.KEY_FCM_TOKEN, "")!!,uid)
        if(avatar != Constants.DEFAULT_AVATAR_URL){
            storageReference.putFile(Uri.parse(userInstance!!.image)).addOnCompleteListener{ putFileTask ->
                if(putFileTask.isSuccessful){
                    storageReference.downloadUrl.addOnSuccessListener { avatarUrl ->
                        userInstance!!.updateImage(avatarUrl.toString())
                        databaseReference.setValue(userInstance).addOnCompleteListener{addUserTask ->
                            if(addUserTask.isSuccessful){
                                _addInformationStatus.postValue(true)
                            } else {
                                _addInformationStatus.postValue(false)
                            }
                        }
                    }
                }
            }
        } else {
            databaseReference.setValue(userInstance).addOnCompleteListener{addUserTask ->
                if(addUserTask.isSuccessful){
                    _addInformationStatus.postValue(true)
                } else {
                    _addInformationStatus.postValue(false)
                }
            }
        }
//        saveAccount(context, email, password)
    }

    private fun saveAccount(context: Context, email: String, password: String){
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("local_data", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("password", password).apply()
    }
}