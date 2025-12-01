package com.example.mam.dto.authentication

import com.google.gson.annotations.SerializedName

data class TwoFaVerifyRequest(
    @SerializedName("secret_key") val secretKey: String,
    @SerializedName("code") val code: String
)