package com.minhtu.firesocialmedia.domain.entity.call

enum class CallType {
    AUDIO,
    VIDEO,
    UNKNOWN
}

fun getCallTypeFromSdp(sdp: String?): CallType {
    return when {
        sdp == null -> CallType.UNKNOWN
        sdp.contains("m=video") -> CallType.VIDEO
        sdp.contains("m=audio") -> CallType.AUDIO
        else -> CallType.UNKNOWN
    }
}
