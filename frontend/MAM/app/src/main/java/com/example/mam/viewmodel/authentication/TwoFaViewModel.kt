package com.example.mam.viewmodel.twofa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mam.MAMApplication
import com.example.mam.repository.TwoFaRepository
import com.example.mam.viewmodel.authentication.otp.TwoFaMode
import com.example.mam.viewmodel.authentication.otp.TwoFaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TwoFaViewModel(
    private val repository: TwoFaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TwoFaState())
    val state = _state.asStateFlow()

    // Hàm lấy thông tin Setup (Gọi API setup-totp)
    fun loadSetupInfo() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Gọi API setup với method mặc định là "APP"
                val response = repository.setupTwoFa(method = "APP")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _state.update {
                        it.copy(
                            isLoading = false,
                            qrCodeUrl = data.qrCodeUrl,
                            secretKey = data.secret
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Lỗi tải QR Code: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Hàm lưu secretKey tạm thời (nếu truyền qua Navigation)
    fun setSecretKey(key: String) {
        _state.update { it.copy(secretKey = key) }
    }

    fun initLoginMode(email: String) {
        _state.update { it.copy(mode = TwoFaMode.LOGIN, emailForLogin = email) }
    }


    // Hàm verify OTP (Gọi API confirm-setup-totp)
    suspend fun verifyOtp(code: String): Int {
        var resultStatus = 0

        _state.update { it.copy(isLoading = true, error = null) }

        try {
            // Gọi API confirm
            val response = repository.confirmTwoFa(code = code, method = "APP")

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                _state.update {
                    it.copy(
                        isLoading = false,
                        recoveryCodes = data.recoveryCodes // Cập nhật list mã khôi phục
                    )
                }
                resultStatus = 1
            } else {
                _state.update { it.copy(isLoading = false, error = "Mã xác thực không đúng") }
                resultStatus = 0
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
            resultStatus = 0
        }
        return resultStatus
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MAMApplication)
                val repository = application.baseRepository.twoFaRepository
                TwoFaViewModel(repository)
            }
        }
    }
}