package com.minhtu.firesocialmedia.domain.serviceimpl.database

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.model.news.BaseNewsInstance
import com.minhtu.firesocialmedia.data.model.news.CommentInstance
import com.minhtu.firesocialmedia.data.model.news.NewsInstance
import com.minhtu.firesocialmedia.data.model.notification.NotificationInstance
import com.minhtu.firesocialmedia.data.model.call.AudioCallSession
import com.minhtu.firesocialmedia.data.model.call.CallStatus
import com.minhtu.firesocialmedia.data.model.call.IceCandidateData
import com.minhtu.firesocialmedia.data.model.call.OfferAnswer
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidDatabaseHelper {
    companion object {
        fun saveInstanceToDatabase(id : String,
                                   path : String,
                                   instance : BaseNewsInstance,
                                   stateFlow : MutableStateFlow<Boolean?>) {
            Log.d("Task", "saveInstanceToDatabase")
            val storageReference = FirebaseStorage.getInstance().getReference()
                .child(path).child(id)
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(instance.image.isNotEmpty()){
                storageReference.putFile(instance.image.toUri()).addOnCompleteListener{ putFileTask ->
                    if(putFileTask.isSuccessful){
                        storageReference.downloadUrl.addOnSuccessListener { dataUrl ->
                            instance.updateImage(dataUrl.toString())
                            databaseReference.setValue(instance).addOnCompleteListener{addUserTask ->
                                stateFlow.value = addUserTask.isSuccessful
                            }
                        }
                    }
                }
            } else {
                if(instance.video.isNotEmpty()) {
                    storageReference.putFile(instance.video.toUri()).addOnCompleteListener{ putFileTask ->
                        if(putFileTask.isSuccessful){
                            storageReference.downloadUrl.addOnSuccessListener { dataUrl ->
                                instance.updateVideo(dataUrl.toString())
                                databaseReference.setValue(instance).addOnCompleteListener{addUserTask ->
                                    stateFlow.value = addUserTask.isSuccessful
                                }
                            }
                        }
                    }
                } else {
                    databaseReference.setValue(instance).addOnCompleteListener{addNewsTask ->
                        stateFlow.value = addNewsTask.isSuccessful
                    }
                }
            }
        }

        fun saveValueToDatabase(id : String,
                                path : String,
                                value : HashMap<String, Int>,
                                externalPath : String,
                                callback : Utils.Companion.BasicCallBack) {
            Log.d("Task", "saveValueToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if(externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if(value.isNotEmpty()){
                databaseReference.setValue(value).addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        callback.onSuccess()
                    } else {
                        callback.onFailure()
                    }
                }
            } else {
                databaseReference.removeValue().addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        callback.onSuccess()
                    } else {
                        callback.onFailure()
                    }
                }
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
            Log.d("Task", "updateCountValueInDatabase:$value")
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
            if(new.image.isNotEmpty() || new.video.isNotEmpty()) {
                FirebaseStorage.getInstance().getReference()
                    .child(path).child(new.id).delete()
            }
        }

        fun deleteCommentFromDatabase(path : String,
                                   comment: CommentInstance) {
            Log.d("Task", "deleteNewsFromDatabase")
            //Delete data in realtime database
            FirebaseDatabase.getInstance().getReference()
                .child(path).child(comment.id).removeValue()
        }

        fun updateNewsFromDatabase(path : String,
                                   newContent : String,
                                   newImage : String,
                                   newVideo : String,
                                   new: NewsInstance,
                                   status: MutableStateFlow<Boolean?>) {
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
                if(newVideo.isNotEmpty()) {
                    if(newVideo != new.video) {
                        storageReference.putFile(newVideo.toUri()).addOnCompleteListener{ putFileTask ->
                            if(putFileTask.isSuccessful){
                                storageReference.downloadUrl.addOnSuccessListener { videoUrl ->
                                    val updates = mapOf<String, Any>(
                                        "message" to newContent,
                                        "video" to videoUrl.toString()
                                    )
                                    updateDataInDatabase(path, new.id, updates, status)
                                }
                            }
                        }
                    } else {
                        val updates = mapOf<String, Any>(
                            "message" to newContent,
                            "video" to newVideo
                        )
                        updateDataInDatabase(path, new.id, updates, status)
                    }
                } else {
                    val updates = mapOf<String, Any>(
                        "message" to newContent,
                        "image" to "",
                        "video" to ""
                    )
                    if(new.image.isNotEmpty() || new.video.isNotEmpty()) {
                        FirebaseStorage.getInstance().getReference()
                            .child(path).child(new.id).delete()
                    }
                    updateDataInDatabase(path, new.id, updates, status)
                }
            }
        }

        private fun updateDataInDatabase(path : String,
                                         id : String,
                                         updates : Map<String, Any>,
                                         status: MutableStateFlow<Boolean?>) {
            FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).updateChildren(updates).addOnCompleteListener {
                        task ->
                    if(task.isSuccessful) {
                        status.value = true
                    } else {
                        status.value = false
                    }
                }
        }

        fun downloadImage(context : Context, image: String, fileName : String, onComplete: (Boolean) -> Unit){
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

        fun sendOfferToFireBase(sessionId : String,
                                offer : OfferAnswer,
                                callPath : String,
                                sendOfferCallBack : Utils.Companion.BasicCallBack) {
            Log.d("Task", "sendOfferToFireBase")
            val offerMap = mapOf(
                "sdp" to offer.sdp,
                "type" to offer.type,
                "initiator" to offer.initiator
            )

            val updates = mutableMapOf<String, Any?>()
            updates["offer"] = offerMap

            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
            database.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendOfferCallBack.onSuccess()
                } else {
                    sendOfferCallBack.onFailure()
                }
            }
        }

        fun sendAnswerToFireBase(
            sessionId : String,
            answer : OfferAnswer,
            callPath : String,
            sendAnswerCallBack : Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "sendAnswerToFireBase")
            val answerMap = mapOf(
                "sdp" to answer.sdp,
                "type" to answer.type,
                "initiator" to answer.initiator
            )

            val updates = mutableMapOf<String, Any?>()
            updates["answer"] = answerMap

            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
            database.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendAnswerCallBack.onSuccess()
                } else {
                    sendAnswerCallBack.onFailure()
                }
            }
        }

        fun updateAnswerInFirebase(
            sessionId: String,
            updateContent: String,
            updateField: String,
            callPath: String,
            updateAnswerCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "updateAnswerInFirebase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("answer").child(updateField)
            database.setValue(updateContent).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateAnswerCallBack.onSuccess()
                } else {
                    updateAnswerCallBack.onFailure()
                }
            }
        }

        fun updateOfferInFirebase(
            sessionId: String,
            updateContent: String,
            updateField: String,
            callPath: String,
            updateOfferCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "updateOfferInFirebase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("offer").child(updateField)
            database.setValue(updateContent).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateOfferCallBack.onSuccess()
                } else {
                    updateOfferCallBack.onFailure()
                }
            }
        }

        fun sendIceCandidateToFireBase(
            sessionId : String,
            iceCandidate: IceCandidateData,
            whichCandidate : String,
            callPath : String,
            sendIceCandidateCallBack : Utils.Companion.BasicCallBack) {
            Log.d("Task", "sendIceCandidateToFireBase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child(whichCandidate)
            database.push().setValue(iceCandidate).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    sendIceCandidateCallBack.onSuccess()
                } else {
                    sendIceCandidateCallBack.onFailure()
                }
            }
        }

        fun sendCallSessionToFirebase(session: AudioCallSession,
                                      callPath : String,
                                      sendCallSessionCallBack : Utils.Companion.BasicCallBack) {
            Log.d("Task", "sendCallSessionToFirebase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(session.sessionId)
            database.setValue(session).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    sendCallSessionCallBack.onSuccess()
                } else {
                    sendCallSessionCallBack.onFailure()
                }
            }
        }

        fun sendCallStatusToFirebase(
                                    sessionId : String,
                                    status: CallStatus,
                                    callPath : String,
                                    sendCallStatusCallBack : Utils.Companion.BasicCallBack) {
            Log.d("Task", "sendCallStatusToFirebase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("status")
            database.setValue(status).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    sendCallStatusCallBack.onSuccess()
                } else {
                    sendCallStatusCallBack.onFailure()
                }
            }
        }

        fun deleteCallSession(
            sessionId: String,
            callPath: String,
            deleteCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "deleteCallSession: $sessionId")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
            database.removeValue().addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    deleteCallBack.onSuccess()
                } else {
                    deleteCallBack.onFailure()
                }
            }
        }

        fun observePhoneCall(
            isInCall : MutableStateFlow<Boolean>,
            currentUserId: String,
            callPath: String,
            phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
            endCallSession: (Boolean) -> Unit,
            iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath)
            firebaseDatabase.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("CallObserver", "onChildAdded")
                    if (isInCall.value) return

                    val offerSnapshot = snapshot.child("offer")
                    val sdp = offerSnapshot.child("sdp").getValue(String::class.java)
                    val type = offerSnapshot.child("type").getValue(String::class.java)

                    val offer = if (sdp != null && type != null) {
                        OfferAnswer(sdp, type)
                    } else null

                    Log.e("CallObserver", "Offer = $offer")

                    if(offer != null) {
                        val audioCallSession = snapshot.getValue(AudioCallSession::class.java) ?: return
                        handleOffer(
                            audioCallSession,
                            offer, 
                            isInCall,
                            currentUserId,
                            endCallSession,
                            phoneCallCallBack, 
                            iceCandidateCallBack)
                    } else {
                        snapshot.ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(updatedSnapshot: DataSnapshot) {
                                val updatedSession = updatedSnapshot.getValue(AudioCallSession::class.java)
                                if (updatedSession?.offer != null) {
                                    handleOffer(
                                        updatedSession,
                                        updatedSession.offer!!,
                                        isInCall,
                                        currentUserId,
                                        endCallSession,
                                        phoneCallCallBack,
                                        iceCandidateCallBack)
                                    snapshot.ref.removeEventListener(this) // Detach after use
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val session = snapshot.getValue(AudioCallSession::class.java) ?: return
                    if (session.calleeId == currentUserId || session.callerId == currentUserId) {
                        // Navigate out of call screen, show message, etc.
                        Log.d("CallObserver", "Call ended by caller or callee")
                        endCallSession(true)
                    }
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe call", error.toException())
                }
            })
        }

        fun observePhoneCallWithoutCheckingInCall(
            currentUserId: String,
            callPath: String,
            phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
            endCallSession: (Boolean) -> Unit,
            iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath)
            firebaseDatabase.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("observePhoneCallWithoutCheckingInCall", "onChildAdded")

                    val offerSnapshot = snapshot.child("offer")
                    val sdp = offerSnapshot.child("sdp").getValue(String::class.java)
                    val type = offerSnapshot.child("type").getValue(String::class.java)

                    val offer = if (sdp != null && type != null) {
                        OfferAnswer(sdp, type)
                    } else null

                    Log.e("observePhoneCallWithoutCheckingInCall", "Offer = $offer")

                    if(offer != null) {
                        val audioCallSession = snapshot.getValue(AudioCallSession::class.java) ?: return
                        handleOfferWithoutInCall(
                            audioCallSession,
                            offer,
                            currentUserId,
                            endCallSession,
                            phoneCallCallBack,
                            iceCandidateCallBack)
                    } else {
                        snapshot.ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(updatedSnapshot: DataSnapshot) {
                                val updatedSession = updatedSnapshot.getValue(AudioCallSession::class.java)
                                if (updatedSession?.offer != null) {
                                    handleOfferWithoutInCall(
                                        updatedSession,
                                        updatedSession.offer!!,
                                        currentUserId,
                                        endCallSession,
                                        phoneCallCallBack,
                                        iceCandidateCallBack)
                                    snapshot.ref.removeEventListener(this) // Detach after use
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val session = snapshot.getValue(AudioCallSession::class.java)
                    if (session?.calleeId == currentUserId || session?.callerId == currentUserId) {
                        // Navigate out of call screen, show message, etc.
                        Log.d("observePhoneCallWithoutCheckingInCall", "Call ended by caller or callee")
                        endCallSession(true)
                    }
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("observePhoneCallWithoutCheckingInCall", "Failed to observe call", error.toException())
                }
            })
        }

        fun handleOffer(session : AudioCallSession?,
                        offer : OfferAnswer,
                        isInCall : MutableStateFlow<Boolean>,
                        currentUserId: String,
                        endCallSession: (Boolean) -> Unit,
                        phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
                        iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit) {
            val sessionId = session?.sessionId
            val callerId = session?.callerId
            val calleeId = session?.calleeId
            val callerCandidate = session?.callerCandidates
            val callStatus = session?.status

            Log.e("CallObserver", sessionId.toString())
            Log.e("CallObserver", callerId.toString())
            Log.e("CallObserver", calleeId.toString())

            if(callStatus != null && callStatus == CallStatus.ENDED) {
                endCallSession(true)
            } else {
                // Only proceed if this user is the callee
                if (calleeId == currentUserId) {
                    val status = session.status
                    isInCall.value = true // Lock state to prevent multiple calls
                    phoneCallCallBack(sessionId.toString(), callerId.toString(), calleeId, offer)
                    iceCandidateCallBack(callerCandidate)
                }
            }
        }

         fun handleOfferWithoutInCall(session : AudioCallSession?,
                                      offer : OfferAnswer,
                                      currentUserId: String,
                                      endCallSession: (Boolean) -> Unit,
                                      phoneCallCallBack : (String, String, String, OfferAnswer) -> Unit,
                                      iceCandidateCallBack : (iceCandidates : Map<String, IceCandidateData>?) -> Unit) {
             val sessionId = session?.sessionId
             val callerId = session?.callerId
             val calleeId = session?.calleeId
             val callerCandidate = session?.callerCandidates
             val callStatus = session?.status

             Log.e("CallObserver", sessionId.toString())
             Log.e("CallObserver", callerId.toString())
             Log.e("CallObserver", calleeId.toString())

             if(callStatus != null && callStatus == CallStatus.ENDED) {
                 endCallSession(true)
             } else {
                 // Only proceed if this user is the callee
                 if (calleeId == currentUserId) {
                     val status = session.status
                     phoneCallCallBack(sessionId.toString(), callerId.toString(), calleeId, offer)
                     iceCandidateCallBack(callerCandidate)
                 }
             }
        }

        private var valueEventListener : ValueEventListener? = null
        fun observeAnswerFromCallee(
            sessionId : String,
            callPath: String,
            answerCallBack : (answer : OfferAnswer) -> Unit,
            rejectCallBack : () -> Unit
        ) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("answer")
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val answer = snapshot.getValue(OfferAnswer::class.java) ?: return
                    val sdp = answer.sdp
                    val type = answer.type
                    val initiator = answer.initiator
                    if (!sdp.isNullOrEmpty()) {
                        Log.e("CallObserver", "Answer received: sdp=$sdp, type=$type")
                        answerCallBack(answer)
                    }
                    if(initiator == "Reject") {
                        rejectCallBack()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe answer", error.toException())
                }
            }
            firebaseDatabase.addValueEventListener(valueEventListener!!)
        }

        //Use this variable to track the call status, prevent invoking function many times.
        private var lastCallStatus : CallStatus? = null
        fun observeCallStatus(
            sessionId: String,
            callPath: String,
            callStatusCallBack: Utils.Companion.CallStatusCallBack
        ) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("status")
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val callStatus = snapshot.getValue(CallStatus::class.java) ?: return
                    if(lastCallStatus != callStatus) {
                        Log.e("observeCallStatus", callStatus.name)
                        lastCallStatus = callStatus
                        if(callStatus == CallStatus.ACCEPTED || callStatus == CallStatus.VIDEO) {
                            callStatusCallBack.onSuccess(callStatus)
                        } else {
                            if(callStatus == CallStatus.ENDED) {
                                callStatusCallBack.onFailure()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe answer", error.toException())
                    callStatusCallBack.onFailure()
                }
            }
            firebaseDatabase.addValueEventListener(valueEventListener!!)
        }

        fun cancelObserveAnswerFromCallee(
            sessionId : String,
            callPath: String
        ) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("answer")
            if(valueEventListener != null) {
                firebaseDatabase.removeEventListener(valueEventListener!!)
            }
        }

        fun observeIceCandidatesFromCallee(
            sessionId: String,
            callPath: String,
            iceCandidateCallBack: (iceCandidate: IceCandidateData) -> Unit
        ) {
            val firebaseDatabase = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)
                .child("calleeCandidates")

            firebaseDatabase.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val iceCandidate = snapshot.getValue(IceCandidateData::class.java) ?: return
                    iceCandidateCallBack(iceCandidate)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("observeIceCandidatesFromCallee", "Failed to observe", error.toException())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            })
        }

        fun observeVideoCall(sessionId: String,
                             callPath: String,
                             videoCallCallBack: (offer : OfferAnswer) -> Unit) {
            val firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId).child("offer")
            firebaseDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offer = snapshot.getValue(OfferAnswer::class.java) ?: return
                    val sdp = offer.sdp
                    val type = offer.type
                    val initiator = offer.initiator
                    if (!sdp.isNullOrEmpty() && sdp.contains("video") && initiator.isNotEmpty()) {
                        Log.e("CallObserver", "Video offer received: sdp=$sdp, type=$type")
                        videoCallCallBack(offer)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe offer", error.toException())
                }
            })
        }
    }
}