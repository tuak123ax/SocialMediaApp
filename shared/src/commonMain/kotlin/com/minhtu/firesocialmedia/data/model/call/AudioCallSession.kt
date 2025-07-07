package com.minhtu.firesocialmedia.data.model.call

enum class CallStatus {
    RINGING, ACCEPTED, ENDED
}
data class AudioCallSession(
    var sessionId: String = "",
    var callerId: String = "",
    var calleeId: String = "",
    var offer: OfferAnswer? = null,
    var answer: OfferAnswer? = null,
    var callerCandidates: Map<String, IceCandidateData> = emptyMap(),
    var calleeCandidates: Map<String, IceCandidateData> = emptyMap(),
    var status: CallStatus = CallStatus.RINGING
)