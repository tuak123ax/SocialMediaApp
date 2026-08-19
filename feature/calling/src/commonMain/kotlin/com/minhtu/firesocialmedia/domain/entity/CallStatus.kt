package com.minhtu.firesocialmedia.domain.entity.call

enum class CallStatus {
    RINGING, ACCEPTED, ENDED, VIDEO
}

interface CallStatusCallBack {
    fun onSuccess(status: CallStatus)
    fun onFailure()
}
