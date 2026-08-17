package com.minhtu.firesocialmedia.android.service.serviceimpl.database.group

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidDatabaseHelper {
    companion object {
        suspend fun saveValueToDatabase(
            id: String,
            path: String,
            value: HashMap<String, Int>,
            externalPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            Log.d("Task", "saveValueToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if (value.isNotEmpty()) {
                databaseReference.setValue(value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true)
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            } else {
                databaseReference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true)
                    } else {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
            Log.d("Task", "Finish saving Value To Database")
        }
    }
}
