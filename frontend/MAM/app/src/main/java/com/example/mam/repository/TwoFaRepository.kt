package com.example.mam.repository

import com.example.mam.dto.authentication.TwoFASetupResponseDTO
import com.example.mam.dto.authentication.VerifySetup2FAResponseDTO
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface TwoFaRepository {

    @POST("2fa/setup-totp")
    suspend fun setupTwoFa(
        @Query("method") method: String = "TOTP"
    ): Response<TwoFASetupResponseDTO>

    @POST("2fa/confirm-setup-totp")
    suspend fun confirmTwoFa(
        @Query("code") code: String,
        @Query("method") method: String = "TOTP"
    ): Response<VerifySetup2FAResponseDTO>
}