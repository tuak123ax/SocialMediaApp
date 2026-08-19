package com.minhtu.firesocialmedia.data.repository.call.profile

import com.minhtu.firesocialmedia.constants.profile.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.ProfileDatabaseService
import com.minhtu.firesocialmedia.domain.repository.call.profile.CallRepository

class CallRepositoryImpl(
    private val databaseService: ProfileDatabaseService
) : CallRepository {
    override suspend fun isCalleeInActiveCall(calleeId: String) : Boolean? {
        return databaseService.anyChildMatchesFieldValue(
            DataConstant.CALL_PATH,
            listOf("calleeId", "callerId"),
            calleeId
        )
    }
}
