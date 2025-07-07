package com.minhtu.firesocialmedia.services.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.UserInstance
import com.minhtu.firesocialmedia.di.FirebaseService
import com.minhtu.firesocialmedia.presentation.signin.SignInState

class AndroidFirebaseService : FirebaseService {
    override fun checkUserExists(email: String, callback: (SignInState) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val databaseReference: DatabaseReference = database.getReference().child("users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var existed = false
                for (dataSnapshot in snapshot.getChildren()) {
                    val user: UserInstance? = dataSnapshot.getValue(UserInstance::class.java)
                    if (user != null) {
                        if(user.email == email){
                            callback.invoke(SignInState(true, Constants.ACCOUNT_EXISTED))
                            existed = true
                        }
                    }
                }
                if(!existed) {
                    callback.invoke(SignInState(true, Constants.ACCOUNT_NOT_EXISTED))
                    Log.e("SignInViewModel","signIn: ACCOUNT_NOT_EXISTED")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.invoke(SignInState(false, Constants.LOGIN_ERROR))
                Log.e("SignInViewModel","signIn: LOGIN_ERROR")
            }
        })
    }


}