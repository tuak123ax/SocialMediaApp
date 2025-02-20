package com.minhtu.firesocialmedia.services.database

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.instance.BaseNewsInstance

class DatabaseHelper {
    companion object {
        fun saveInstanceToDatabase(id : String,
                           path : String,
                           instance : BaseNewsInstance,
                           liveData : MutableLiveData<Boolean>) {
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child(path).child(id)
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(instance.image.isNotEmpty()){
                storageReference.putFile(Uri.parse(instance.image)).addOnCompleteListener{ putFileTask ->
                    if(putFileTask.isSuccessful){
                        storageReference.downloadUrl.addOnSuccessListener { imageUrl ->
                            instance.updateImage(imageUrl.toString())
                            databaseReference.setValue(instance).addOnCompleteListener{addUserTask ->
                                if(addUserTask.isSuccessful){
                                    liveData.postValue(true)
                                } else {
                                    liveData.postValue(false)
                                }
                            }
                        }
                    }
                }
            } else {
                databaseReference.setValue(instance).addOnCompleteListener{addNewsTask ->
                    if(addNewsTask.isSuccessful){
                        liveData.postValue(true)
                    } else {
                        liveData.postValue(false)
                    }
                }
            }
        }

        fun saveValueToDatabase(id : String,
                                path : String,
                                value : HashMap<String, Boolean>,
                                externalPath : String) {
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if(value.isNotEmpty()){
                databaseReference.setValue(value)
            } else {
                databaseReference.removeValue()
            }
        }
    }
}