package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.auth.data.remote.dto.user.UserDTO
import com.minhtu.firesocialmedia.constants.auth.DataConstant
import com.minhtu.firesocialmedia.data.remote.dto.settings.auth.IpInfoResponseDTO
import com.minhtu.firesocialmedia.data.remote.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.testutil.fakeIpInfoRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAuthDatabaseService : AuthDatabaseService {
    var savedUserId: String? = null
    var savedLocationInfo: IpInfoResponseDTO? = null
    var savedHistoryPath: String? = null
    var savedLoginHistoryPath: String? = null

    override suspend fun saveLoginActivityInfo(
        userId: String,
        locationInfo: IpInfoResponseDTO,
        historyPath: String,
        loginHistoryPath: String
    ) {
        savedUserId = userId
        savedLocationInfo = locationInfo
        savedHistoryPath = historyPath
        savedLoginHistoryPath = loginHistoryPath
    }
    override suspend fun getUser(userId: String): UserDTO? = null
    override suspend fun saveSignUpInformation(user: UserDTO): Boolean = true
}

class SaveLoginActivityInfoUseCaseTest {
    @Test
    fun `invoke fetches approximate location and saves login activity with the right paths`() = runTest {
        val authDb = FakeAuthDatabaseService()
        val useCase = SaveLoginActivityInfoUseCase(authDb, fakeIpInfoRemoteDataSource())

        useCase.invoke("u1")

        assertEquals("u1", authDb.savedUserId)
        assertEquals("Somewhere, US", authDb.savedLocationInfo?.locationInfo())
        assertEquals(DataConstant.HISTORY_PATH, authDb.savedHistoryPath)
        assertEquals(DataConstant.LOGIN_PATH, authDb.savedLoginHistoryPath)
    }
}
