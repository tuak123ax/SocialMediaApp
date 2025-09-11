package com.minhtu.firesocialmedia.domain.entity.call

import com.minhtu.firesocialmedia.data.dto.call.OfferAnswerDTO

enum class CallStatus {
    RINGING, ACCEPTED, ENDED, VIDEO
}
data class AudioCallSession(
    var sessionId: String = "",
    var callerId: String = "",
    var calleeId: String = "",
    var offer: OfferAnswerDTO? = null,
    var answer: OfferAnswerDTO? = null,
    var callerCandidates: Map<String, IceCandidateData> = emptyMap(),
    var calleeCandidates: Map<String, IceCandidateData> = emptyMap(),
    var status: CallStatus = CallStatus.RINGING
)