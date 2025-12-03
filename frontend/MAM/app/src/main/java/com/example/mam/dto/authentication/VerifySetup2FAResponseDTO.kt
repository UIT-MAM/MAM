package com.example.mam.dto.authentication

import com.google.gson.annotations.SerializedName

data class VerifySetup2FAResponseDTO(
    @SerializedName("recoveryCodes") val recoveryCodes: List<String>?
)