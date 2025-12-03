package com.example.mam.viewmodel.authentication.twofa

import com.example.mam.viewmodel.authentication.twofa.TwoFaMode

data class TwoFaState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mode: TwoFaMode = TwoFaMode.SETUP, // Mặc định là Setup

    // Dữ liệu cho Setup
    val qrCodeUrl: String = "",
    val secretKey: String = "",
    val recoveryCodes: List<String> = emptyList(),

    // Dữ liệu cho Login
    val pendingToken: String = "" // Token tạm khi login yêu cầu 2FA
)