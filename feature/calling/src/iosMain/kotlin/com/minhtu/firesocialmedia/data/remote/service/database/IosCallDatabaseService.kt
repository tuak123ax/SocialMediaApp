package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.calling.utils.Utils as CallingUtils
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallStatusDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import kotlinx.coroutines.flow.MutableStateFlow

class IosCallDatabaseService : CallDatabaseService {
    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatusDTO
    ): Boolean {
        // iOS implementation will be added later
        return false
    }

    override suspend fun deleteCallSession(
        sessionId: String
    ): Boolean {
        // iOS implementation will be added later
        return false
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateDTO>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (Map<String, IceCandidateDTO>?) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun clearAnswerInFirebase(
        sessionId: String
    ) {
        // iOS implementation will be added later
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: CallingUtils.Companion.CallStatusCallBack
    ) {
        // iOS implementation will be added later
    }

    override suspend fun cancelObserveAnswerFromCallee(
        sessionId: String,
        callPath: String
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (IceCandidateDTO) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (OfferAnswerDTO) -> Unit
    ) {
        // iOS implementation will be added later
    }

    override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean {
        // iOS implementation will be added later
        return false
    }

    override fun stopObservePhoneCall() {
        // iOS implementation will be added later
    }

    override fun stopObservePhoneCallWithoutCheckingInCall() {
        // iOS implementation will be added later
    }
}
