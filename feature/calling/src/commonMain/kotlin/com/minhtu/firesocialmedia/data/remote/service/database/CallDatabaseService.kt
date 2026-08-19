package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.calling.utils.Utils as CallingUtils
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallStatusDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import kotlinx.coroutines.flow.MutableStateFlow

interface CallDatabaseService {
    suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatusDTO
    ): Boolean

    suspend fun deleteCallSession(sessionId: String): Boolean

    suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    )

    suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    )

    suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun clearAnswerInFirebase(
        sessionId: String
    )

    suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    )

    suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (answer: OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    )

    suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: CallingUtils.Companion.CallStatusCallBack
    )

    suspend fun cancelObserveAnswerFromCallee(
        sessionId: String,
        callPath: String
    )

    suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
    )

    suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer: OfferAnswerDTO) -> Unit
    )

    suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean

    fun stopObservePhoneCall()

    /** Removes only the observer from [observePhoneCallWithoutCheckingInCall]. Use when call ends in service so the app's incoming-call observer is not removed. */
    fun stopObservePhoneCallWithoutCheckingInCall()
}
