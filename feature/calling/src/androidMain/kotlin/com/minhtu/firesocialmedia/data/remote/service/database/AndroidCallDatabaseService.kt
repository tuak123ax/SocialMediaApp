package com.minhtu.firesocialmedia.data.remote.service.database

import com.minhtu.firesocialmedia.calling.utils.Utils as CallingUtils
import com.minhtu.firesocialmedia.calling.utils.Utils
import com.minhtu.firesocialmedia.constants.calling.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.call.AudioCallSessionDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallStatusDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.CallingRequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.IceCandidateDTO
import com.minhtu.firesocialmedia.data.remote.dto.call.OfferAnswerDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume

class AndroidCallDatabaseService : CallDatabaseService {
    override suspend fun sendOfferToFireBase(
        sessionId: String,
        offer: OfferAnswerDTO,
        sendOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.sendOfferToFireBase(
            sessionId,
            offer,
            DataConstant.CALL_PATH,
            sendOfferCallBack
        )
    }

    override suspend fun sendIceCandidateToFireBase(
        sessionId: String,
        iceCandidate: IceCandidateDTO,
        whichCandidate: String,
        sendIceCandidateCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.sendIceCandidateToFireBase(
            sessionId,
            iceCandidate,
            whichCandidate,
            DataConstant.CALL_PATH,
            sendIceCandidateCallBack
        )
    }

    override suspend fun sendCallSessionToFirebase(
        session: AudioCallSessionDTO,
        sendCallSessionCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.sendCallSessionToFirebase(
            session,
            DataConstant.CALL_PATH,
            sendCallSessionCallBack
        )
    }

    override suspend fun sendCallStatusToFirebase(
        sessionId: String,
        status: CallStatusDTO
    ): Boolean {
        return AndroidCallDatabaseHelper.sendCallStatusToFirebase(
            sessionId,
            status,
            DataConstant.CALL_PATH
        )
    }

    override suspend fun deleteCallSession(sessionId: String): Boolean {
        return AndroidCallDatabaseHelper.deleteCallSession(sessionId, DataConstant.CALL_PATH)
    }

    override suspend fun observePhoneCall(
        isInCall: MutableStateFlow<Boolean>,
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    ) {
        AndroidCallDatabaseHelper.observePhoneCall(
            isInCall,
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack
        )
    }

    override suspend fun observePhoneCallWithoutCheckingInCall(
        currentUserId: String,
        phoneCallCallBack: (CallingRequestDTO) -> Unit,
        endCallSession: (Boolean) -> Unit,
        whoEndCallCallBack: (String) -> Unit,
        iceCandidateCallBack: (iceCandidates: Map<String, IceCandidateDTO>?) -> Unit
    ) {
        AndroidCallDatabaseHelper.observePhoneCallWithoutCheckingInCall(
            currentUserId,
            DataConstant.CALL_PATH,
            phoneCallCallBack,
            endCallSession,
            whoEndCallCallBack,
            iceCandidateCallBack
        )
    }

    override suspend fun sendAnswerToFirebase(
        sessionId: String,
        answer: OfferAnswerDTO,
        sendAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.sendAnswerToFireBase(
            sessionId,
            answer,
            DataConstant.CALL_PATH,
            sendAnswerCallBack
        )
    }

    override suspend fun updateAnswerInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateAnswerCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.updateAnswerInFirebase(
            sessionId,
            updateContent,
            updateField,
            DataConstant.CALL_PATH,
            updateAnswerCallBack
        )
    }

    override suspend fun clearAnswerInFirebase(
        sessionId: String
    ) = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        AndroidCallDatabaseHelper.clearAnswerInFirebase(
            sessionId,
            DataConstant.CALL_PATH,
            object : Utils.Companion.BasicCallBack {
                override fun onSuccess() {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onFailure() {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        )
    }

    override suspend fun updateOfferInFirebase(
        sessionId: String,
        updateContent: String,
        updateField: String,
        updateOfferCallBack: Utils.Companion.BasicCallBack
    ) {
        AndroidCallDatabaseHelper.updateOfferInFirebase(
            sessionId,
            updateContent,
            updateField,
            DataConstant.CALL_PATH,
            updateOfferCallBack
        )
    }

    override suspend fun observeAnswerFromCallee(
        sessionId: String,
        answerCallBack: (answer: OfferAnswerDTO) -> Unit,
        rejectCallBack: () -> Unit
    ) {
        AndroidCallDatabaseHelper.observeAnswerFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            answerCallBack,
            rejectCallBack
        )
    }

    override suspend fun observeCallStatus(
        sessionId: String,
        callStatusCallBack: CallingUtils.Companion.CallStatusCallBack
    ) {
        AndroidCallDatabaseHelper.observeCallStatus(
            sessionId,
            DataConstant.CALL_PATH,
            callStatusCallBack
        )
    }

    override suspend fun cancelObserveAnswerFromCallee(sessionId: String, callPath: String) {
        AndroidCallDatabaseHelper.cancelObserveAnswerFromCallee(
            sessionId,
            callPath
        )
    }

    override suspend fun observeIceCandidatesFromCallee(
        sessionId: String,
        iceCandidateCallBack: (iceCandidate: IceCandidateDTO) -> Unit
    ) {
        AndroidCallDatabaseHelper.observeIceCandidatesFromCallee(
            sessionId,
            DataConstant.CALL_PATH,
            iceCandidateCallBack
        )
    }

    override suspend fun observeVideoCall(
        sessionId: String,
        videoCallCallBack: (offer: OfferAnswerDTO) -> Unit
    ) {
        AndroidCallDatabaseHelper.observeVideoCall(
            sessionId,
            DataConstant.CALL_PATH,
            videoCallCallBack
        )
    }

    override suspend fun sendWhoEndCall(sessionId: String, whoEndCall: String): Boolean {
        return AndroidCallDatabaseHelper.sendWhoEndCall(sessionId, whoEndCall, DataConstant.CALL_PATH)
    }

    override fun stopObservePhoneCall() {
        AndroidCallDatabaseHelper.stopObservePhoneCall()
    }

    override fun stopObservePhoneCallWithoutCheckingInCall() {
        AndroidCallDatabaseHelper.stopObservePhoneCallWithoutCheckingInCall()
    }
}
