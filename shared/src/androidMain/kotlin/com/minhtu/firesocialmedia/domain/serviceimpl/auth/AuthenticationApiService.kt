package com.minhtu.firesocialmedia.domain.serviceimpl.auth

import com.minhtu.firesocialmedia.constants.Constants.Companion.APP_SCRIPT_2FA_ENDPOINT
import com.minhtu.firesocialmedia.data.remote.dto.authentication.TwoFARequestDTO
import com.minhtu.firesocialmedia.data.remote.dto.authentication.TwoFAResponseDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApiService {

    @POST(APP_SCRIPT_2FA_ENDPOINT)
    fun sendVerifyRequestToAppScript(
        @Body request: TwoFARequestDTO
    ): Call<TwoFAResponseDTO>
}