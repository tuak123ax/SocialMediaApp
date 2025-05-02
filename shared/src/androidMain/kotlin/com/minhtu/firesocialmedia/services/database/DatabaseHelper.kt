package com.minhtu.firesocialmedia.services.database

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.instance.BaseNewsInstance
import com.minhtu.firesocialmedia.instance.NotificationInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.google.firebase.database.GenericTypeIndicator
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.instance.NewsInstance
import org.json.JSONArray
import org.json.JSONObject

class DatabaseHelper {
    companion object {
        fun saveInstanceToDatabase(id : String,
                                   path : String,
                                   instance : BaseNewsInstance,
                                   liveData : MutableLiveData<Boolean>) {
            Log.d("Task", "saveInstanceToDatabase")
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child(path).child(id)
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(instance.image.isNotEmpty()){
                storageReference.putFile(instance.image.toUri()).addOnCompleteListener{ putFileTask ->
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
            Log.d("Task", "saveValueToDatabase")
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
            Log.d("Task", "Finish saving Value To Database")
        }

        suspend fun saveListToDatabase(id : String,
                                path : String,
                                value : ArrayList<String>,
                                externalPath : String) {
            Log.d("Task", "saveListToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            withContext(Dispatchers.Main) {
                databaseReference.setValue(value)
            }
            Log.d("Task", "Finish saving List To Database")
        }

        suspend fun saveStringToDatabase(id : String,
                                path : String,
                                value : String,
                                externalPath : String) {
            Log.d("Task", "saveStringToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if(value.isNotEmpty()){
                withContext(Dispatchers.Main) {
                    databaseReference.setValue(value)
                }
            }
        }

        fun updateCountValueInDatabase(id : String,
                                path : String,
                                externalPath : String, value : Int) {
            Log.d("Task", "updateCountValueInDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if(value >= 0) {
                databaseReference.setValue(value)
            }
        }

        fun saveNotificationToDatabase(id : String,
                                   path : String,
                                   instance : ArrayList<NotificationInstance>) {
            Log.d("Task", "saveNotificationToDatabase")
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).child(Constants.NOTIFICATION_PATH)
            databaseReference.setValue(instance)
            }

        fun deleteNotificationFromDatabase(id : String,
                                           path : String,
                                           notification: NotificationInstance) {
            Log.d("Task", "deleteNotificationFromDatabase")
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).child(Constants.NOTIFICATION_PATH)
            databaseReference.get().addOnSuccessListener { snapshot ->
                //Get notification list from db
                val list = snapshot.getValue(object : GenericTypeIndicator<List<NotificationInstance>>() {})?.toMutableList()
                //Delete value in notification list and upload the list to db again
                list?.let {
                    it.remove(notification) // or any value
                    databaseReference.setValue(it) // overwrite with updated list
                }
            }
        }

        fun deleteNewsFromDatabase(path : String,
                                   new: NewsInstance) {
            Log.d("Task", "deleteNewsFromDatabase")
            //Delete data in realtime database
            FirebaseDatabase.getInstance().getReference()
                .child(path).child(new.id).removeValue()
            //Delete data in firebase storage
            if(new.image.isNotEmpty()) {
                FirebaseStorage.getInstance().getReference()
                    .child(path).child(new.id).delete()
            }
        }

        fun updateNewsFromDatabase(path : String,
                                   newContent : String,
                                   newImage : String,
                                   new: NewsInstance,
                                   status: MutableLiveData<Boolean>) {
            Log.d("Task", "updateNewsFromDatabase")
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child(path).child(new.id)
            if(newImage.isNotEmpty()){
                if(newImage != new.image) {
                    storageReference.putFile(newImage.toUri()).addOnCompleteListener{ putFileTask ->
                        if(putFileTask.isSuccessful){
                            storageReference.downloadUrl.addOnSuccessListener { imageUrl ->
                                val updates = mapOf<String, Any>(
                                    "message" to newContent,
                                    "image" to imageUrl.toString()
                                )
                                updateDataInDatabase(path, new.id, updates, status)
                            }
                        }
                    }
                } else {
                    val updates = mapOf<String, Any>(
                        "message" to newContent,
                        "image" to newImage
                    )
                    updateDataInDatabase(path, new.id, updates, status)
                }
            } else {
                val updates = mapOf<String, Any>(
                    "message" to newContent,
                    "image" to ""
                )
                if(new.image.isNotEmpty()) {
                    FirebaseStorage.getInstance().getReference()
                        .child(path).child(new.id).delete()
                }
                updateDataInDatabase(path, new.id, updates, status)
            }
        }

        private fun updateDataInDatabase(path : String, id : String, updates : Map<String, Any>, status: MutableLiveData<Boolean>) {
            FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).updateChildren(updates).addOnCompleteListener {
                        task ->
                    if(task.isSuccessful) {
                        status.postValue(true)
                    } else {
                        status.postValue(false)
                    }
                }
        }

        fun downloadImage(context : Context, image: String, fileName : String){
            val request = DownloadManager.Request(image.toUri())
                .setTitle("Download Image")
                .setDescription("Downloading $fileName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }
    }
}