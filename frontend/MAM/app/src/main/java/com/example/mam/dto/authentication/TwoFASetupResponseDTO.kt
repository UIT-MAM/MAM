package com.example.mam.dto.authentication

import com.google.gson.annotations.SerializedName

data class TwoFASetupResponseDTO(
    @SerializedName("secret") val secretKey: String?,
    @SerializedName("qrCodeUrl") val qrCodeUrl: String?
)