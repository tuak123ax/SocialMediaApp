package com.minhtu.firesocialmedia.domain.serviceimpl.database

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.core.uri.Uri
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.minhtu.firesocialmedia.constants.Constants
import com.minhtu.firesocialmedia.data.remote.constant.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupDTO
import com.minhtu.firesocialmedia.data.remote.dto.group.GroupSummaryDTO
import com.minhtu.firesocialmedia.data.remote.dto.notification.NotificationDTO
import com.minhtu.firesocialmedia.data.remote.dto.settings.SessionItemDTO
import com.minhtu.firesocialmedia.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.call.CallStatus
import com.minhtu.firesocialmedia.domain.serviceimpl.database.supabase.SupabaseStorageHelper.Companion.resolveMediaUrlAsync
import com.minhtu.firesocialmedia.platform.logMessage
import com.minhtu.firesocialmedia.utils.Utils
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                        if (continuation.isActive) continuation.resume(true, onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(false, onCancellation = {})
                    }
                }
            } else {
                databaseReference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) continuation.resume(true, onCancellation = {})
                    } else {
                        if (continuation.isActive) continuation.resume(false, onCancellation = {})
                    }
                }
            }
            Log.d("Task", "Finish saving Value To Database")
        }

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

        suspend fun saveStringToDatabase(
            id: String,
            path: String,
            value: String,
            externalPath: String
        ) {
            Log.d("Task", "saveStringToDatabase")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if (value.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    databaseReference.setValue(value)
                }
            }
        }

        fun updateCountValueInDatabase(
            id: String,
            path: String,
            externalPath: String, value: Int
        ) {
            Log.d("Task", "updateCountValueInDatabase:$value")
            var databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id)
            if (externalPath.isNotEmpty()) {
                databaseReference = databaseReference.child(externalPath)
            }
            if (value >= 0) {
                databaseReference.setValue(value)
            }
        }

        fun saveNotificationToDatabase(
            id: String,
            path: String,
            instance: ArrayList<NotificationDTO>
        ) {
            Log.d("Task", "saveNotificationToDatabase")
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).child(DataConstant.NOTIFICATION_PATH)
            databaseReference.setValue(instance)
        }

        fun deleteNotificationFromDatabase(
            id: String,
            path: String,
            notification: NotificationDTO
        ) {
            Log.d("Task", "deleteNotificationFromDatabase")
            val databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(path).child(id).child(DataConstant.NOTIFICATION_PATH)
            databaseReference.get().addOnSuccessListener { snapshot ->
                //Get notification list from db
                val list =
                    snapshot.getValue(object : GenericTypeIndicator<List<NotificationDTO>>() {})
                        ?.toMutableList()
                //Delete value in notification list and upload the list to db again
                list?.let {
                    it.remove(notification) // or any value
                    databaseReference.setValue(it) // overwrite with updated list
                }
            }
        }

        suspend fun deleteCommentFromDatabase(
            path: String,
            comment: BaseNewsInstance
        ) {
            Log.d("Task", "deleteNewsFromDatabase")
            //Delete data in realtime database
            FirebaseDatabase.getInstance().getReference()
                .child(path).child(comment.id).removeValue().await()
        }

        suspend fun downloadImage(context: Context, image: String, fileName: String): Boolean =
            suspendCancellableCoroutine { continuation ->
                val request = DownloadManager.Request(image.toUri())
                    .setTitle("Download Image")
                    .setDescription("Downloading $fileName")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val result = downloadManager.enqueue(request)
                if (result == -1L) {
                    if (continuation.isActive) continuation.resume(false, onCancellation = {})
                } else {
                    if (continuation.isActive) continuation.resume(true, onCancellation = {})
                }
            }

        fun sendOfferToFireBase(
            sessionId: String,
            offer: OfferAnswerDTO,
            callPath: String,
            sendOfferCallBack: Utils.Companion.BasicCallBack
        ) {
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
            sessionId: String,
            answer: OfferAnswerDTO,
            callPath: String,
            sendAnswerCallBack: Utils.Companion.BasicCallBack
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
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
                .child("answer").child(updateField)
            database.setValue(updateContent).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateAnswerCallBack.onSuccess()
                } else {
                    updateAnswerCallBack.onFailure()
                }
            }
        }

        fun clearAnswerInFirebase(
            sessionId: String,
            callPath: String,
            clearAnswerCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "clearAnswerInFirebase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
                .child("answer")
            database.setValue(null).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    clearAnswerCallBack.onSuccess()
                } else {
                    clearAnswerCallBack.onFailure()
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
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
                .child("offer").child(updateField)
            database.setValue(updateContent).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateOfferCallBack.onSuccess()
                } else {
                    updateOfferCallBack.onFailure()
                }
            }
        }

        fun sendIceCandidateToFireBase(
            sessionId: String,
            iceCandidate: IceCandidateDTO,
            whichCandidate: String,
            callPath: String,
            sendIceCandidateCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "sendIceCandidateToFireBase")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
                .child(whichCandidate)
            database.push().setValue(iceCandidate).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendIceCandidateCallBack.onSuccess()
                } else {
                    sendIceCandidateCallBack.onFailure()
                }
            }
        }

        fun sendCallSessionToFirebase(
            session: AudioCallSessionDTO,
            callPath: String,
            sendCallSessionCallBack: Utils.Companion.BasicCallBack
        ) {
            Log.d("Task", "sendCallSessionToFirebase")
            val database =
                FirebaseDatabase.getInstance().getReference(callPath).child(session.sessionId)
            database.setValue(session).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendCallSessionCallBack.onSuccess()
                } else {
                    sendCallSessionCallBack.onFailure()
                }
            }
        }

        suspend fun sendCallStatusToFirebase(
            sessionId: String,
            status: CallStatus,
            callPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            Log.d("Task", "sendCallStatusToFirebase")
            val sessionRef = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)

            sessionRef.get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        sessionRef.child("status").setValue(status)
                            .addOnCompleteListener { t ->
                                if (t.isSuccessful) {
                                    if (continuation.isActive) continuation.resume(
                                        true,
                                        onCancellation = {})
                                } else {
                                    if (continuation.isActive) continuation.resume(
                                        false,
                                        onCancellation = {})
                                }
                            }
                    } else {
                        if (continuation.isActive) continuation.resume(false, onCancellation = {})
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(
                        false,
                        onCancellation = {})
                }
        }

        suspend fun sendWhoEndCall(
            sessionId: String,
            whoEndCall: String,
            callPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            Log.d("Task", "sendWhoEndCall: $sessionId $whoEndCall")
            val sessionRef = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)

            sessionRef.get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        sessionRef.child("whoEndCall").setValue(whoEndCall)
                            .addOnCompleteListener { t ->
                                if (t.isSuccessful) {
                                    Log.d("Task", "sendWhoEndCall success")
                                    if (continuation.isActive) continuation.resume(
                                        true,
                                        onCancellation = {})
                                } else {
                                    if (continuation.isActive) continuation.resume(
                                        false,
                                        onCancellation = {})
                                }
                            }
                    } else {
                        if (continuation.isActive) continuation.resume(false, onCancellation = {})
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(
                        false,
                        onCancellation = {})
                }
        }

        suspend fun deleteCallSession(
            sessionId: String,
            callPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            Log.d("Task", "deleteCallSession: $sessionId")
            val database = FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
            database.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (continuation.isActive) continuation.resume(true, onCancellation = {})
                } else {
                    if (continuation.isActive) continuation.resume(false, onCancellation = {})
                }
            }
        }

        private var callListener: ChildEventListener? = null
        private var firebaseDatabase: DatabaseReference? = null
        private var callListenerWithoutInCallCheck: ChildEventListener? = null
        private var firebaseDatabaseWithoutInCallCheck: DatabaseReference? = null

        // Track listeners that must be removed in stopObserve* to prevent leaks
        private val pendingOfferListeners =
            mutableListOf<Pair<DatabaseReference, ValueEventListener>>()
        private val pendingOfferListenersWithoutInCall =
            mutableListOf<Pair<DatabaseReference, ValueEventListener>>()
        private val callerCandidatesListeners =
            mutableListOf<Pair<DatabaseReference, ChildEventListener>>()
        private val callerCandidatesListenersWithoutInCall =
            mutableListOf<Pair<DatabaseReference, ChildEventListener>>()
        private var calleeCandidatesRef: DatabaseReference? = null
        private var calleeCandidatesListener: ChildEventListener? = null
        private var videoOfferRef: DatabaseReference? = null
        private var videoOfferListener: ValueEventListener? = null
        fun observePhoneCall(
            isInCall: MutableStateFlow<Boolean>,
            currentUserId: String,
            callPath: String,
            phoneCallCallBack: (CallingRequestDTO) -> Unit,
            endCallSession: (Boolean) -> Unit,
            whoEndCallCallBack: (String) -> Unit,
            iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
        ) {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference(callPath)
            callListener?.let { firebaseDatabase!!.removeEventListener(it) }

            // Keep track of what we've already handled
            val handled = mutableSetOf<String>()

            callListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("CallObserver", "onChildAdded")
                    processData(
                        snapshot,
                        callPath,
                        isInCall,
                        currentUserId,
                        handled,
                        phoneCallCallBack,
                        iceCandidateCallBack
                    )
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("CallObserver", "onChildChanged")
                    processData(
                        snapshot,
                        callPath,
                        isInCall,
                        currentUserId,
                        handled,
                        phoneCallCallBack,
                        iceCandidateCallBack
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val session = snapshot.getValue(AudioCallSessionDTO::class.java) ?: return
                    if (session.calleeId == currentUserId || session.callerId == currentUserId) {
                        // Navigate out of call screen, show message, etc.
                        Log.d("CallObserver", "Call ended by caller or callee")
                        whoEndCallCallBack(session.whoEndCall)
                        endCallSession(true)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe call", error.toException())
                }
            }
            firebaseDatabase!!.addChildEventListener(callListener!!)
        }

        private fun processData(
            snapshot: DataSnapshot,
            callPath: String,
            isInCall: MutableStateFlow<Boolean>,
            currentUserId: String,
            handled: MutableSet<String>,
            phoneCallCallBack: (CallingRequestDTO) -> Unit,
            iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
        ) {
            Log.e("CallObserver", "isInCall: ${isInCall.value}")
            if (isInCall.value) return
            val offerSnapshot = snapshot.child("offer")
            val sdp = offerSnapshot.child("sdp").getValue(String::class.java)
            val type = offerSnapshot.child("type").getValue(String::class.java)
            val offer = if (sdp != null && type != null) {
                OfferAnswerDTO(sdp, type)
            } else null

            if (offer != null) {
                // Dedupe by child key + offer SDP (or add a createdAt/callId field and use that)
                val key = "${snapshot.key}:${offer.sdp}"
                if (!handled.add(key)) return
                val audioCallSession = snapshot.getValue(AudioCallSessionDTO::class.java) ?: return
                handleOffer(
                    audioCallSession,
                    offer,
                    callPath,
                    isInCall,
                    currentUserId,
                    phoneCallCallBack,
                    iceCandidateCallBack
                )
            } else {
                val ref = snapshot.ref
                val listener = object : ValueEventListener {
                    override fun onDataChange(updatedSnapshot: DataSnapshot) {
                        val updatedSession =
                            updatedSnapshot.getValue(AudioCallSessionDTO::class.java)
                        if (updatedSession?.offer != null) {
                            synchronized(pendingOfferListeners) {
                                pendingOfferListeners.removeAll { it.first == ref }
                            }
                            ref.removeEventListener(this)
                            handleOffer(
                                updatedSession,
                                updatedSession.offer!!,
                                callPath,
                                isInCall,
                                currentUserId,
                                phoneCallCallBack,
                                iceCandidateCallBack
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        synchronized(pendingOfferListeners) {
                            pendingOfferListeners.removeAll { it.first == ref }
                        }
                        ref.removeEventListener(this)
                    }
                }
                synchronized(pendingOfferListeners) {
                    pendingOfferListeners.add(ref to listener)
                }
                ref.addValueEventListener(listener)
            }
        }

        fun observePhoneCallWithoutCheckingInCall(
            currentUserId: String,
            callPath: String,
            phoneCallCallBack: (CallingRequestDTO) -> Unit,
            endCallSession: (Boolean) -> Unit,
            whoEndCallCallBack: (String) -> Unit,
            iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
        ) {
            // Remove any previous listener so we don't have multiple active observers (e.g. from a prior call).
            callListenerWithoutInCallCheck?.let {
                firebaseDatabaseWithoutInCallCheck?.removeEventListener(
                    it
                )
            }
            callListenerWithoutInCallCheck = null
            firebaseDatabaseWithoutInCallCheck =
                FirebaseDatabase.getInstance().getReference(callPath)
            callListenerWithoutInCallCheck = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("observePhoneCallWithoutCheckingInCall", "onChildAdded")

                    val offerSnapshot = snapshot.child("offer")
                    val sdp = offerSnapshot.child("sdp").getValue(String::class.java)
                    val type = offerSnapshot.child("type").getValue(String::class.java)

                    val offer = if (sdp != null && type != null) {
                        OfferAnswerDTO(sdp, type)
                    } else null

                    if (offer != null) {
                        val audioCallSession =
                            snapshot.getValue(AudioCallSessionDTO::class.java) ?: return
                        handleOfferWithoutInCall(
                            audioCallSession,
                            offer,
                            callPath,
                            currentUserId,
                            phoneCallCallBack,
                            iceCandidateCallBack
                        )
                    } else {
                        val ref = snapshot.ref
                        val listener = object : ValueEventListener {
                            override fun onDataChange(updatedSnapshot: DataSnapshot) {
                                val updatedSession =
                                    updatedSnapshot.getValue(AudioCallSessionDTO::class.java)
                                if (updatedSession?.offer != null) {
                                    synchronized(pendingOfferListenersWithoutInCall) {
                                        pendingOfferListenersWithoutInCall.removeAll { it.first == ref }
                                    }
                                    ref.removeEventListener(this)
                                    handleOfferWithoutInCall(
                                        updatedSession,
                                        updatedSession.offer!!,
                                        callPath,
                                        currentUserId,
                                        phoneCallCallBack,
                                        iceCandidateCallBack
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                synchronized(pendingOfferListenersWithoutInCall) {
                                    pendingOfferListenersWithoutInCall.removeAll { it.first == ref }
                                }
                                ref.removeEventListener(this)
                            }
                        }
                        synchronized(pendingOfferListenersWithoutInCall) {
                            pendingOfferListenersWithoutInCall.add(ref to listener)
                        }
                        ref.addValueEventListener(listener)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val session = snapshot.getValue(AudioCallSessionDTO::class.java)
                    if (session?.calleeId == currentUserId || session?.callerId == currentUserId) {
                        // Navigate out of call screen, show message, etc.
                        Log.d(
                            "observePhoneCallWithoutCheckingInCall",
                            "Call ended by caller or callee"
                        )
                        whoEndCallCallBack(session.whoEndCall)
                        endCallSession(true)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "observePhoneCallWithoutCheckingInCall",
                        "Failed to observe call",
                        error.toException()
                    )
                }
            }
            firebaseDatabaseWithoutInCallCheck!!.addChildEventListener(
                callListenerWithoutInCallCheck!!
            )
        }

        fun handleOffer(
            session: AudioCallSessionDTO?,
            offer: OfferAnswerDTO,
            callPath: String,
            isInCall: MutableStateFlow<Boolean>,
            currentUserId: String,
            phoneCallCallBack: (CallingRequestDTO) -> Unit,
            iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
        ) {
            val sessionId = session?.sessionId
            val callerId = session?.callerId
            val calleeId = session?.calleeId
            val callerCandidate = session?.callerCandidates
            val callStatus = session?.status
            // Only proceed if this user is the callee AND the call is actually ringing
            if (calleeId == currentUserId && callStatus == CallStatus.RINGING) {
                isInCall.value = true // Lock state to prevent multiple calls
                phoneCallCallBack(
                    CallingRequestDTO(
                        sessionId.toString(),
                        callerId.toString(),
                        calleeId,
                        offer
                    )
                )
                iceCandidateCallBack(callerCandidate)

                // Observe live caller ICE candidates and forward incrementally
                if (!sessionId.isNullOrEmpty()) {
                    val candidatesRef = FirebaseDatabase.getInstance()
                        .getReference(callPath)
                        .child(sessionId)
                        .child("callerCandidates")

                    val listener = object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val ice = snapshot.getValue(IceCandidateDTO::class.java) ?: return
                            val single = HashMap<String, IceCandidateDTO>()
                            single[snapshot.key ?: System.currentTimeMillis().toString()] = ice
                            iceCandidateCallBack(single)
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    }
                    synchronized(callerCandidatesListeners) {
                        callerCandidatesListeners.add(candidatesRef to listener)
                    }
                    candidatesRef.addChildEventListener(listener)
                }
            }
        }

        fun handleOfferWithoutInCall(
            session: AudioCallSessionDTO?,
            offer: OfferAnswerDTO,
            callPath: String,
            currentUserId: String,
            phoneCallCallBack: (CallingRequestDTO) -> Unit,
            iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
        ) {
            val sessionId = session?.sessionId
            val callerId = session?.callerId
            val calleeId = session?.calleeId
            val callerCandidate = session?.callerCandidates
            val callStatus = session?.status
            // Only proceed if this user is the callee
            if (calleeId == currentUserId) {
                phoneCallCallBack(
                    CallingRequestDTO(
                        sessionId.toString(),
                        callerId.toString(),
                        calleeId,
                        offer
                    )
                )
                iceCandidateCallBack(callerCandidate)

                // Observe live caller ICE candidates and forward incrementally
                if (!sessionId.isNullOrEmpty()) {
                    val candidatesRef = FirebaseDatabase.getInstance()
                        .getReference(callPath)
                        .child(sessionId)
                        .child("callerCandidates")

                    val listener = object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val ice = snapshot.getValue(IceCandidateDTO::class.java) ?: return
                            val single = HashMap<String, IceCandidateDTO>()
                            single[snapshot.key ?: System.currentTimeMillis().toString()] = ice
                            iceCandidateCallBack(single)
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    }
                    synchronized(callerCandidatesListenersWithoutInCall) {
                        callerCandidatesListenersWithoutInCall.add(candidatesRef to listener)
                    }
                    candidatesRef.addChildEventListener(listener)
                }
            }
        }

        // Distinct listener refs to avoid cross-removals and leaks
        private var answerValueEventListener: ValueEventListener? = null
        private var callStatusValueEventListener: ValueEventListener? = null
        private var answerDatabaseRef: DatabaseReference? = null
        private var callStatusDatabaseRef: DatabaseReference? = null
        fun observeAnswerFromCallee(
            sessionId: String,
            callPath: String,
            answerCallBack: (answer: OfferAnswerDTO) -> Unit,
            rejectCallBack: () -> Unit
        ) {
            // Clean up any previous answer listener before attaching a new one
            answerDatabaseRef?.let { ref ->
                answerValueEventListener?.let { ref.removeEventListener(it) }
            }
            answerDatabaseRef = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)
                .child("answer")
            answerValueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val answer = snapshot.getValue(OfferAnswerDTO::class.java) ?: return
                    val sdp = answer.sdp
                    val type = answer.type
                    val initiator = answer.initiator
                    // When callee declines video call, only initiator is set to "Reject" (sdp may still be present from a previous write). Treat reject first so we never show "accepted" toast.
                    if (initiator == "Reject") {
                        rejectCallBack()
                        return
                    }
                    if (!sdp.isNullOrEmpty()) {
                        Log.e("CallObserver", "Answer received: sdp=$sdp, type=$type")
                        answerCallBack(answer)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe answer", error.toException())
                }
            }
            answerDatabaseRef!!.addValueEventListener(answerValueEventListener!!)
        }

        //Use this variable to track the call status, prevent invoking function many times.
        private var lastCallStatus: CallStatus? = null
        fun observeCallStatus(
            sessionId: String,
            callPath: String,
            callStatusCallBack: Utils.Companion.CallStatusCallBack
        ) {
            // Reset so the next call does not see stale status from the previous call (e.g. ACCEPTED/ENDED)
            lastCallStatus = null
            // Clean up any previous status listener before attaching a new one
            callStatusDatabaseRef?.let { ref ->
                callStatusValueEventListener?.let { ref.removeEventListener(it) }
            }
            callStatusDatabaseRef = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)
                .child("status")
            callStatusValueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Session deleted (callee ended without writing ENDED) → treat as call ended so caller dismisses notification
                    if (!snapshot.exists()) {
                        if (lastCallStatus != null) {
                            Log.e("observeCallStatus", "session removed (call ended)")
                            callStatusCallBack.onFailure()
                            callStatusDatabaseRef?.let { ref ->
                                callStatusValueEventListener?.let { ref.removeEventListener(it) }
                            }
                            callStatusValueEventListener = null
                            callStatusDatabaseRef = null
                        }
                        return
                    }
                    val callStatus = snapshot.getValue(CallStatus::class.java) ?: return
                    if (lastCallStatus != callStatus) {
                        Log.e("observeCallStatus", callStatus.name)
                        if (callStatus == CallStatus.ACCEPTED || callStatus == CallStatus.VIDEO) {
                            lastCallStatus = callStatus
                            callStatusCallBack.onSuccess(callStatus)
                            // Once accepted or switched to video, we no longer need to observe status here
                            callStatusDatabaseRef?.let { ref ->
                                callStatusValueEventListener?.let { ref.removeEventListener(it) }
                            }
                            callStatusValueEventListener = null
                            callStatusDatabaseRef = null
                        } else if (callStatus == CallStatus.ENDED) {
                            // Only treat ENDED as "call ended" if we had seen this call start (RINGING/ACCEPTED).
                            // Otherwise it's stale from the previous call — ignore so next call gets RINGING first.
                            val hadCallStarted = lastCallStatus != null
                            lastCallStatus = callStatus
                            if (hadCallStarted) {
                                callStatusCallBack.onFailure()
                                callStatusDatabaseRef?.let { ref ->
                                    callStatusValueEventListener?.let { ref.removeEventListener(it) }
                                }
                                callStatusValueEventListener = null
                                callStatusDatabaseRef = null
                            }
                        } else {
                            lastCallStatus = callStatus
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CallObserver", "Failed to observe answer", error.toException())
                    callStatusCallBack.onFailure()
                    callStatusDatabaseRef?.let { ref ->
                        callStatusValueEventListener?.let { ref.removeEventListener(it) }
                    }
                    callStatusValueEventListener = null
                    callStatusDatabaseRef = null
                }
            }
            callStatusDatabaseRef!!.addValueEventListener(callStatusValueEventListener!!)
        }

        fun cancelObserveAnswerFromCallee(
            sessionId: String,
            callPath: String
        ) {
            // Remove current answer listener if present
            answerDatabaseRef?.let { ref ->
                answerValueEventListener?.let { ref.removeEventListener(it) }
            }
            answerValueEventListener = null
            answerDatabaseRef = null
        }

        fun observeIceCandidatesFromCallee(
            sessionId: String,
            callPath: String,
            iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
        ) {
            calleeCandidatesRef?.let { ref ->
                calleeCandidatesListener?.let { ref.removeEventListener(it) }
            }
            calleeCandidatesRef = null
            calleeCandidatesListener = null

            val firebaseDatabase = FirebaseDatabase.getInstance()
                .getReference(callPath)
                .child(sessionId)
                .child("calleeCandidates")
            calleeCandidatesRef = firebaseDatabase
            calleeCandidatesListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("observeIceCandidatesFromCallee", "Got ice candidate from callee")
                    val iceCandidate = snapshot.getValue(IceCandidateDTO::class.java) ?: return
                    iceCandidateCallBack(iceCandidate)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "observeIceCandidatesFromCallee",
                        "Failed to observe",
                        error.toException()
                    )
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            }
            firebaseDatabase.addChildEventListener(calleeCandidatesListener!!)
        }

        fun observeVideoCall(
            sessionId: String,
            callPath: String,
            videoCallCallBack: (offer: OfferAnswerDTO) -> Unit
        ) {
            videoOfferRef?.let { ref ->
                videoOfferListener?.let { ref.removeEventListener(it) }
            }
            videoOfferRef = null
            videoOfferListener = null

            val firebaseDatabase =
                FirebaseDatabase.getInstance().getReference(callPath).child(sessionId)
                    .child("offer")
            videoOfferRef = firebaseDatabase
            videoOfferListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offer = snapshot.getValue(OfferAnswerDTO::class.java) ?: return
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
            }
            firebaseDatabase.addValueEventListener(videoOfferListener!!)
        }

        fun stopObservePhoneCall() {
            callListener?.let { l -> firebaseDatabase?.removeEventListener(l) }
            callListener = null
            firebaseDatabase = null
            callListenerWithoutInCallCheck?.let { l ->
                firebaseDatabaseWithoutInCallCheck?.removeEventListener(
                    l
                )
            }
            callListenerWithoutInCallCheck = null
            firebaseDatabaseWithoutInCallCheck = null
            // Remove pending offer listeners (waiting for offer on session refs)
            synchronized(pendingOfferListeners) {
                pendingOfferListeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }
                pendingOfferListeners.clear()
            }
            synchronized(pendingOfferListenersWithoutInCall) {
                pendingOfferListenersWithoutInCall.forEach { (ref, listener) ->
                    ref.removeEventListener(
                        listener
                    )
                }
                pendingOfferListenersWithoutInCall.clear()
            }
            // Remove callerCandidates listeners
            synchronized(callerCandidatesListeners) {
                callerCandidatesListeners.forEach { (ref, listener) ->
                    ref.removeEventListener(
                        listener
                    )
                }
                callerCandidatesListeners.clear()
            }
            synchronized(callerCandidatesListenersWithoutInCall) {
                callerCandidatesListenersWithoutInCall.forEach { (ref, listener) ->
                    ref.removeEventListener(
                        listener
                    )
                }
                callerCandidatesListenersWithoutInCall.clear()
            }
            calleeCandidatesRef?.let { ref ->
                calleeCandidatesListener?.let { ref.removeEventListener(it) }
            }
            calleeCandidatesRef = null
            calleeCandidatesListener = null
            videoOfferRef?.let { ref ->
                videoOfferListener?.let { ref.removeEventListener(it) }
            }
            videoOfferRef = null
            videoOfferListener = null
            // Also stop answer and call status listeners to avoid leaks
            answerDatabaseRef?.let { ref ->
                answerValueEventListener?.let { ref.removeEventListener(it) }
            }
            callStatusDatabaseRef?.let { ref ->
                callStatusValueEventListener?.let { ref.removeEventListener(it) }
            }
            answerValueEventListener = null
            callStatusValueEventListener = null
            answerDatabaseRef = null
            callStatusDatabaseRef = null
        }

        fun stopObservePhoneCallWithoutCheckingInCall() {
            callListenerWithoutInCallCheck?.let { l ->
                firebaseDatabaseWithoutInCallCheck?.removeEventListener(
                    l
                )
            }
            callListenerWithoutInCallCheck = null
            firebaseDatabaseWithoutInCallCheck = null
            synchronized(pendingOfferListenersWithoutInCall) {
                pendingOfferListenersWithoutInCall.forEach { (ref, listener) ->
                    ref.removeEventListener(
                        listener
                    )
                }
                pendingOfferListenersWithoutInCall.clear()
            }
            synchronized(callerCandidatesListenersWithoutInCall) {
                callerCandidatesListenersWithoutInCall.forEach { (ref, listener) ->
                    ref.removeEventListener(
                        listener
                    )
                }
                callerCandidatesListenersWithoutInCall.clear()
            }
            // Also clear per-call signaling listeners so a completed/aborted call does not
            // leak answer/video/status updates into the next negotiation.
            calleeCandidatesRef?.let { ref ->
                calleeCandidatesListener?.let { ref.removeEventListener(it) }
            }
            calleeCandidatesRef = null
            calleeCandidatesListener = null
            videoOfferRef?.let { ref ->
                videoOfferListener?.let { ref.removeEventListener(it) }
            }
            videoOfferRef = null
            videoOfferListener = null
            answerDatabaseRef?.let { ref ->
                answerValueEventListener?.let { ref.removeEventListener(it) }
            }
            answerDatabaseRef = null
            answerValueEventListener = null
            callStatusDatabaseRef?.let { ref ->
                callStatusValueEventListener?.let { ref.removeEventListener(it) }
            }
            callStatusDatabaseRef = null
            callStatusValueEventListener = null
        }

        suspend fun uploadMediaAndGetUrl(
            storageRef: StorageReference,
            originalUriStr: String?,
            localPath: String?,
            contentType: String? = null
        ): String {
            val metaBuilder = StorageMetadata.Builder()
                .setCacheControl("public,max-age=604800,immutable")
            if (contentType != null) {
                metaBuilder.setContentType(contentType)
            }
            val metadata = metaBuilder.build()

            // Attempt 1: original URI string (if parseable)
            val firstUri = originalUriStr?.let {
                runCatching { it.toUri() }.getOrNull()
            }

            if (firstUri != null) {
                runCatching {
                    storageRef.putFile(firstUri, metadata).await()
                }.onSuccess {
                    return storageRef.downloadUrl.await().toString()
                }
            }

            // Attempt 2: local file path
            val localFile = localPath?.let { File(it) }
            if (localFile != null && localFile.exists()) {
                val fileUri = Uri.fromFile(localFile)
                storageRef.putFile(fileUri, metadata).await()
                return storageRef.downloadUrl.await().toString()
            }

            // Nothing worked
            error("No readable source for upload (uri=$originalUriStr, localPath=$localPath)")
        }

        fun updateGroupDataOnServer(
            databaseRef: DatabaseReference,
            groupRootPath: String,
            userRootPath: String,
            userGroupsField: String,
            group: GroupDTO,
            userId: String,
            continuation: CancellableContinuation<Boolean>
        ) {
            // Store only necessary fields under user
            val groupSummary = GroupSummaryDTO(
                id = group.id,
                name = group.name,
                avatar = group.avatar
            )

            val updates = hashMapOf<String, Any?>(
                "$groupRootPath/${group.id}" to group,

                "$userRootPath/$userId/$userGroupsField/${group.id}" to groupSummary
            )

            databaseRef.updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    if (!task.isSuccessful) {
                        Log.e("Task", "updateChildren FAILED", task.exception)
                        Log.e("Task", "updates=$updates")
                    } else {
                        Log.d("Task", "updateChildren SUCCESS")
                    }

                    continuation.resume(task.isSuccessful, onCancellation = {})
                }
        }

        suspend fun getAllGroups(
            userPath: String,
            groupPath: String,
            userId: String
        ): Set<GroupSummaryDTO> {

            val snapshot = FirebaseDatabase
                .getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(groupPath)
                .get()
                .await()

            return snapshot.children
                .mapNotNull { it.getValue(GroupSummaryDTO::class.java) }
                .let { groups ->
                    coroutineScope {
                        groups.map { group ->
                            async { group.copy(avatar = resolveMediaUrlAsync(group.avatar)) }
                        }.awaitAll()
                    }
                }
                .toSet()
        }

        suspend fun fetchGroupInfo(
            groupId: String,
            groupPath: String
        ): GroupDTO? {

            val snapshot = FirebaseDatabase
                .getInstance()
                .reference
                .child(groupPath)
                .child(groupId)
                .get()
                .await()

            val group = snapshot.getValue(GroupDTO::class.java)

            return group?.copy(
                avatar = resolveMediaUrlAsync(group.avatar)
            )
        }

        suspend fun updateNotificationStatus(
            newStatus: Boolean,
            groupId: String,
            userId: String,
            userPath: String,
            groupPath: String,
            notificationStatusPath: String
        ): Boolean {
            return runCatching {
                val databaseRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(userPath)
                    .child(userId)
                    .child(groupPath)
                    .child(groupId)
                    .child(notificationStatusPath)
                databaseRef.setValue(newStatus).await()
                true
            }.getOrElse {
                false
            }
        }

        suspend fun getAllMembersInGroup(
            groupId: String,
            groupPath: String,
            membersPath: String
        ): HashMap<String, String> {
            return runCatching {
                val snapshot = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(groupPath)
                    .child(groupId)
                    .child(membersPath)
                    .get()
                    .await()

                val result = HashMap<String, String>()

                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.getValue(String::class.java) ?: continue
                    result[key] = value
                }

                result
            }.getOrElse {
                HashMap()
            }
        }

        suspend fun getGroupConfigs(
            userId: String,
            groupId: String,
            userPath: String,
            groupPath: String
        ): GroupSummaryDTO {

            return try {
                val snapshot = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(userPath)
                    .child(userId)
                    .child(groupPath)
                    .child(groupId)
                    .get()
                    .await()

                val group = snapshot.getValue(GroupSummaryDTO::class.java)

                group?.copy(
                    avatar = resolveMediaUrlAsync(group.avatar)
                ) ?: GroupSummaryDTO()

            } catch (e: Exception) {
                GroupSummaryDTO()
            }
        }

        suspend fun fetchNotificationState(
            userId: String,
            groupId: String,
            userPath: String,
            groupPath: String,
            notificationStatusPath: String
        ): Boolean {
            return runCatching {
                val snapshot = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(userPath)
                    .child(userId)
                    .child(groupPath)
                    .child(groupId)
                    .child(notificationStatusPath)
                    .get()
                    .await()
                snapshot.getValue(Boolean::class.java) ?: false
            }.getOrElse {
                false
            }
        }

        fun inviteFriendToGroup(
            friendDto: UserDTO,
            userPath: String,
            notificationPath: String
        ) {
            val databaseRef = FirebaseDatabase
                .getInstance()
                .reference
                .child(userPath)
                .child(friendDto.uid)
                .child(notificationPath)
            databaseRef.setValue(friendDto.notifications)
        }

        suspend fun addUserToGroup(
            user: UserDTO,
            group: GroupDTO,
            userPath: String,
            groupPath: String,
            memberPath: String,
            memberCountPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->

            val databaseRef = FirebaseDatabase.getInstance().reference

            val groupSummary = GroupSummaryDTO(
                id = group.id,
                name = group.name,
                avatar = group.avatar  // store raw path, not resolved URL
            )

            val updates = hashMapOf<String, Any?>(
                "$groupPath/${group.id}/$memberPath/${user.uid}" to "member",

                "$userPath/${user.uid}/$groupPath/${group.id}" to groupSummary,

                "$groupPath/${group.id}/$memberCountPath" to ServerValue.increment(1)
            )

            databaseRef.updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    if (!task.isSuccessful) {
                        Log.e("Task", "updateChildren FAILED", task.exception)
                        Log.e("Task", "updates=$updates")
                    } else {
                        Log.d("Task", "updateChildren SUCCESS")
                    }

                    continuation.resume(task.isSuccessful, onCancellation = {})
                }
        }

        suspend fun removeUserFromGroup(
            user: UserDTO,
            group: GroupDTO,
            userPath: String,
            groupPath: String,
            memberPath: String,
            memberCountPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            val databaseRef = FirebaseDatabase.getInstance().reference


            val updates = hashMapOf(
                "$groupPath/${group.id}/$memberPath/${user.uid}" to null,

                "$userPath/${user.uid}/$groupPath/${group.id}" to null,
                "$groupPath/${group.id}/$memberCountPath" to ServerValue.increment(-1)
            )

            databaseRef.updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    if (!task.isSuccessful) {
                        Log.e("Task", "updateChildren FAILED", task.exception)
                        Log.e("Task", "updates=$updates")
                    } else {
                        Log.d("Task", "updateChildren SUCCESS")
                    }

                    continuation.resume(task.isSuccessful, onCancellation = {})
                }
        }

        suspend fun deleteGroup(
            user: UserDTO,
            group: GroupDTO,
            userPath: String,
            groupPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            val databaseRef = FirebaseDatabase.getInstance().reference


            val updates = hashMapOf<String, Any?>(
                "$groupPath/${group.id}" to null,

                "$userPath/${user.uid}/$groupPath/${group.id}" to null
            )

            databaseRef.updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    if (!task.isSuccessful) {
                        Log.e("Task", "updateChildren FAILED", task.exception)
                        Log.e("Task", "updates=$updates")
                    } else {
                        Log.d("Task", "updateChildren SUCCESS")
                    }

                    continuation.resume(task.isSuccessful, onCancellation = {})
                }
        }

        suspend fun updateMemberRole(
            role: String,
            user: UserDTO,
            group: GroupDTO,
            groupPath: String,
            memberPath: String
        ): Boolean = suspendCancellableCoroutine { continuation ->
            val databaseRef = FirebaseDatabase
                .getInstance()
                .reference
                .child(groupPath)
                .child(group.id)
                .child(memberPath)
                .child(user.uid)
            databaseRef.setValue(role).addOnCompleteListener { task ->
                continuation.resume(task.isSuccessful, onCancellation = {})
            }
        }

        suspend fun fetchGroupsByMemberCount(
            limit: Int,
            groupPath: String,
            memberCountPath: String
        ): List<GroupDTO> = suspendCancellableCoroutine { continuation ->

            val databaseRef = FirebaseDatabase
                .getInstance()
                .reference
                .child(groupPath)

            databaseRef
                .orderByChild(memberCountPath)
                .limitToLast(limit)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val rawList = snapshot.children
                            .mapNotNull { it.getValue(GroupDTO::class.java) }
                            .sortedByDescending { it.memberCount }

                        if (continuation.isActive) {
                            continuation.resume(rawList, onCancellation = {})
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (continuation.isActive) {
                            continuation.resume(emptyList(), onCancellation = {})
                        }
                    }
                })
        }

        fun updateIsReadStatusOfNotification(
            userId: String,
            notificationId: String,
            userPath: String,
            notificationPath: String
        ) {
            val notificationsRef = FirebaseDatabase
                .getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(notificationPath)

            notificationsRef.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { child ->
                    val id = child.child("id").getValue(String::class.java)
                    if (id == notificationId) {
                        child.ref.child("beRead").setValue(true)
                        return@addOnSuccessListener
                    }
                }
            }
        }

        suspend fun deleteAllNotifications(
            uid: String,
            userPath: String,
            notificationPath: String
        ): Result<Unit> {
            return try {
                FirebaseDatabase.getInstance()
                    .reference
                    .child(userPath)
                    .child(uid)
                    .child(notificationPath)
                    .removeValue()
                    .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun updateTwoFAEnabledFlagForUser(
            userId: String,
            twoFAEnabled: Boolean,
            userPath: String,
            twoFaEnabledPath: String
        ): Boolean {
            val ref = FirebaseDatabase.getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(twoFaEnabledPath)

            var delayTime = 200L

            repeat(3) { attempt ->
                try {
                    withTimeout(3000) {
                        ref.setValue(twoFAEnabled).await()
                    }
                    return true
                } catch (e: Exception) {
                    val shouldRetry = e is IOException

                    if (attempt < 2 && shouldRetry) {
                        delay(delayTime)
                        delayTime *= 2
                    } else {
                        Log.e("Firebase", "Failed to update 2FA flag", e)
                        return false
                    }
                }
            }

            return false
        }

        suspend fun fetchLoginHistoryList(
            userId: String,
            historyPath: String,
            loginHistoryPath: String
        ): List<SessionItemDTO> {
            return runCatching {
                val ref = FirebaseDatabase
                    .getInstance()
                    .reference
                    .child(historyPath)
                    .child(loginHistoryPath)
                    .child(userId)

                Log.d("fetchLoginHistoryList", "Fetching login history...")
                Log.d("fetchLoginHistoryList", "UserId: $userId")

                val snapshot = ref.get().await()

                Log.d("fetchLoginHistoryList", "Snapshot exists: ${snapshot.exists()}")
                Log.d("fetchLoginHistoryList", "Children count: ${snapshot.childrenCount}")

                val result = snapshot.children.mapNotNull { child ->
                    Log.d("fetchLoginHistoryList", "Raw child key: ${child.key}")
                    Log.d("fetchLoginHistoryList", "Raw value: ${child.value}")

                    val item = child.getValue(SessionItemDTO::class.java)

                    if (item == null) {
                        Log.e("fetchLoginHistoryList", "Failed to parse child: ${child.key}")
                    } else {
                        Log.d("fetchLoginHistoryList", "Parsed item: $item")
                    }

                    item
                }

                Log.d("fetchLoginHistoryList", "Final list size: ${result.size}")

                result
            }.onFailure { e ->
                Log.e("fetchLoginHistoryList", "Error fetching login history", e)
            }.getOrElse {
                emptyList()
            }
        }

        suspend fun saveLoginActivityInfo(
            context: Context,
            userId: String,
            historyPath: String,
            loginHistoryPath: String
        ) {
            try {
                val sessionItemDTO = prepareSessionItemData(context)
                val dbRef = FirebaseDatabase
                    .getInstance()
                    .reference
                    .child(historyPath)
                    .child(loginHistoryPath)
                    .child(userId)

                dbRef.push().setValue(sessionItemDTO).await()
            } catch(e : Exception) {
                logMessage("saveLoginActivityInfo", { "Exception happened: ${e.message}" })
            }
        }

        private fun prepareSessionItemData(context: Context) : SessionItemDTO {
            val sessionId = java.util.UUID.randomUUID().toString()
            saveLocalSessionId(context, sessionId)
            val deviceName = getDeviceName()
            val location = getLocation(context)
            val timeMillis = System.currentTimeMillis()
            return SessionItemDTO(
                sessionId = sessionId,
                deviceName = deviceName,
                location = location,
                time = timeMillis
            )
        }

        fun saveLocalSessionId(context: Context, sessionId: String) {
            context.getSharedPreferences("session_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.KEY_SESSION_ID, sessionId)
                .apply()
        }

        fun getLocalSessionId(context: Context): String {
            return context.getSharedPreferences("session_prefs", android.content.Context.MODE_PRIVATE)
                .getString(Constants.KEY_SESSION_ID, "") ?: ""
        }

        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer, ignoreCase = true)) {
                model.replaceFirstChar { it.uppercase() }
            } else {
                "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
            }
        }

        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getLocation(context: Context): String {
            val locale = context.resources.configuration.locales[0]
            return "${locale.country}"
        }

        suspend fun updateUserLongField(
            userId: String,
            fieldPath: String,
            value: Long,
            userPath: String
        ): Boolean {
            val ref = FirebaseDatabase.getInstance()
                .reference
                .child(userPath)
                .child(userId)
                .child(fieldPath)

            var delayTime = 200L

            repeat(3) { attempt ->
                try {
                    withTimeout(3000) {
                        ref.setValue(value).await()
                    }
                    return true
                } catch (e: Exception) {
                    val shouldRetry = e is IOException

                    if (attempt < 2 && shouldRetry) {
                        delay(delayTime)
                        delayTime *= 2
                    } else {
                        Log.e("Firebase", "Failed to update user long field", e)
                        return false
                    }
                }
            }

            return false
        }
    }
}