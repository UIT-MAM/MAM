package com.example.mam.dto.authentication

import com.google.gson.annotations.SerializedName

data class TwoFASetupResponseDTO(
    @SerializedName("secret_key") val secret: String,
    @SerializedName("qr_code_url") val qrCodeUrl: String
)