package com.minhtu.firesocialmedia.domain.repository.call.profile

interface CallRepository {
    suspend fun isCalleeInActiveCall(calleeId: String): Boolean?
}
