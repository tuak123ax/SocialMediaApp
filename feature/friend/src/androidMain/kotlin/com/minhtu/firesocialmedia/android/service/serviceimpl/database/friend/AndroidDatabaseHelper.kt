package com.minhtu.firesocialmedia.android.service.serviceimpl.database.friend

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDatabaseHelper {
    companion object {
        suspend fun saveListToDatabase(
            id: String,
            path: String,
            value: ArrayList<String>,
            externalPath: String
        ) {
            Log.d("Task", "saveListToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            withContext(Dispatchers.Main) {
                databaseReference.setValue(value)
            }
            Log.d("Task", "Finish saving List To Database")
        }
    }
}
