package com.minhtu.firesocialmedia.data.remote.service.database

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minhtu.firesocialmedia.calling.utils.Utils as CallingUtils
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallStatusDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Real Firebase Realtime Database logic for calling-domain operations, extracted from
 * core's AndroidDatabaseHelper as part of moving calling DTOs/logic to feature/calling.
 */
object AndroidCallDatabaseHelper {

        private fun clearListener(ref: DatabaseReference?, listener: ValueEventListener?) {
            if (ref != null && listener != null) {
                ref.removeEventListener(listener)
            }
        }

        private fun clearListener(ref: DatabaseReference?, listener: ChildEventListener?) {
            if (ref != null && listener != null) {
                ref.removeEventListener(listener)
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
            status: CallStatusDTO,
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
                        if (continuation.isActive) continuation.resume(false)
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
                        if (continuation.isActive) continuation.resume(false)
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
                    if (continuation.isActive) continuation.resume(true)
                } else {
                    if (continuation.isActive) continuation.resume(false)
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
            if (calleeId == currentUserId && callStatus == CallStatusDTO.RINGING) {
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
            clearListener(answerDatabaseRef, answerValueEventListener)
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
        private var lastCallStatus: CallStatusDTO? = null
        fun observeCallStatus(
            sessionId: String,
            callPath: String,
            callStatusCallBack: CallingUtils.Companion.CallStatusCallBack
        ) {
            // Reset so the next call does not see stale status from the previous call (e.g. ACCEPTED/ENDED)
            lastCallStatus = null
            // Clean up any previous status listener before attaching a new one
            clearListener(callStatusDatabaseRef, callStatusValueEventListener)
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
                            clearListener(callStatusDatabaseRef, callStatusValueEventListener)
                            callStatusValueEventListener = null
                            callStatusDatabaseRef = null
                        }
                        return
                    }
                    val callStatus = snapshot.getValue(CallStatusDTO::class.java) ?: return
                    if (lastCallStatus != callStatus) {
                        Log.e("observeCallStatus", callStatus.name)
                        if (callStatus == CallStatusDTO.ACCEPTED || callStatus == CallStatusDTO.VIDEO) {
                            lastCallStatus = callStatus
                            callStatusCallBack.onSuccess(callStatus)
                            // Once accepted or switched to video, we no longer need to observe status here
                            clearListener(callStatusDatabaseRef, callStatusValueEventListener)
                            callStatusValueEventListener = null
                            callStatusDatabaseRef = null
                        } else if (callStatus == CallStatusDTO.ENDED) {
                            // Only treat ENDED as "call ended" if we had seen this call start (RINGING/ACCEPTED).
                            // Otherwise it's stale from the previous call — ignore so next call gets RINGING first.
                            val hadCallStarted = lastCallStatus != null
                            lastCallStatus = callStatus
                            if (hadCallStarted) {
                                callStatusCallBack.onFailure()
                                clearListener(callStatusDatabaseRef, callStatusValueEventListener)
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
                    clearListener(callStatusDatabaseRef, callStatusValueEventListener)
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
            clearListener(answerDatabaseRef, answerValueEventListener)
            answerValueEventListener = null
            answerDatabaseRef = null
        }

        fun observeIceCandidatesFromCallee(
            sessionId: String,
            callPath: String,
            iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
        ) {
            clearListener(calleeCandidatesRef, calleeCandidatesListener)
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
            clearListener(videoOfferRef, videoOfferListener)
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
            clearListener(calleeCandidatesRef, calleeCandidatesListener)
            calleeCandidatesRef = null
            calleeCandidatesListener = null
            clearListener(videoOfferRef, videoOfferListener)
            videoOfferRef = null
            videoOfferListener = null
            // Also stop answer and call status listeners to avoid leaks
            clearListener(answerDatabaseRef, answerValueEventListener)
            clearListener(callStatusDatabaseRef, callStatusValueEventListener)
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
            clearListener(calleeCandidatesRef, calleeCandidatesListener)
            calleeCandidatesRef = null
            calleeCandidatesListener = null
            clearListener(videoOfferRef, videoOfferListener)
            videoOfferRef = null
            videoOfferListener = null
            clearListener(answerDatabaseRef, answerValueEventListener)
            answerDatabaseRef = null
            answerValueEventListener = null
    }
}
