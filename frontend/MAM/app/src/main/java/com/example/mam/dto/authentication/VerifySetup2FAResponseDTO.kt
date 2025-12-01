package com.example.mam.dto.authentication

import com.google.gson.annotations.SerializedName

data class VerifySetup2FAResponseDTO(
    @SerializedName("recovery_codes") val recoveryCodes: List<String>
)