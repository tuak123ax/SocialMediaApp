package com.minhtu.firesocialmedia.domain.usecases.signin

import com.minhtu.firesocialmedia.constants.auth.DataConstant
import com.minhtu.firesocialmedia.data.remote.service.database.AuthDatabaseService
import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource

class SaveLoginActivityInfoUseCase(
    private val authDatabaseService: AuthDatabaseService,
    private val ipInfoRemoteDataSource: IpInfoRemoteDataSource
) {
    suspend operator fun invoke(userId: String) {
        val locationInfo = ipInfoRemoteDataSource.getApproximateLocation()
        authDatabaseService.saveLoginActivityInfo(
            userId,
            locationInfo,
            DataConstant.HISTORY_PATH,
            DataConstant.LOGIN_PATH
        )
    }
}
