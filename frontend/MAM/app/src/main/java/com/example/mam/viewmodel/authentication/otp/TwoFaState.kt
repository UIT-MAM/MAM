package com.example.mam.viewmodel.authentication.otp

enum class TwoFaMode { SETUP, LOGIN }
data class TwoFaState(
    val isLoading: Boolean = false,
    val qrCodeUrl: String = "",
    val secretKey: String = "",
    val recoveryCodes: List<String> = emptyList(),
    val error: String? = null,
    val mode: TwoFaMode = TwoFaMode.SETUP,
    val emailForLogin: String = ""
)