package com.example.mam.viewmodel.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mam.MAMApplication
import com.example.mam.data.UserPreferencesRepository
import com.example.mam.repository.AuthPublicRepository
import com.example.mam.repository.TwoFaRepository
import com.example.mam.viewmodel.authentication.twofa.TwoFaMode
import com.example.mam.viewmodel.authentication.twofa.TwoFaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TwoFaViewModel(
    private val twoFaRepository: TwoFaRepository,
    private val authRepository: AuthPublicRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TwoFaState())
    val state = _state.asStateFlow()

    // =========================================================================
    // PHẦN 1: SETUP 2FA (Khi user đã đăng nhập và muốn bật 2FA)
    // =========================================================================

    // Gọi API lấy thông tin QR Code
    fun loadSetupInfo() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, mode = TwoFaMode.SETUP) }
            try {
                Log.d("TwoFaCheck", "Bắt đầu gọi API setup...") // <--- LOG 1

                // Đảm bảo method là "TOTP"
                val response = twoFaRepository.setupTwoFa(method = "TOTP")

                Log.d("TwoFaCheck", "Code: ${response.code()}") // <--- LOG 2

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // <--- LOG 3: In ra xem server trả về gì --->
                    Log.d("TwoFaCheck", "Body trả về: Secret=${data.secretKey}, QR=${data.qrCodeUrl}")

                    _state.update {
                        it.copy(
                            isLoading = false,
                            qrCodeUrl = data.qrCodeUrl ?: "",
                            secretKey = data.secretKey ?: ""
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TwoFaCheck", "Lỗi API: $errorBody") // <--- LOG 4
                    _state.update { it.copy(isLoading = false, error = "Lỗi: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e("TwoFaCheck", "Exception: ${e.message}") // <--- LOG 5
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Xác thực OTP để hoàn tất Setup (Nhận về mã khôi phục)
    private suspend fun verifySetupOtp(code: String): Int {
        var resultStatus = 0
        _state.update { it.copy(isLoading = true, error = null) }

        try {
            val response = twoFaRepository.confirmTwoFa(code = code, method = "TOTP")

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                _state.update {
                    it.copy(
                        isLoading = false,
                        recoveryCodes = data.recoveryCodes ?: emptyList()
                    )
                }
                resultStatus = 1 // Thành công -> UI chuyển sang màn hiện mã khôi phục
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

    // =========================================================================
    // PHẦN 2: LOGIN 2FA (Khi user đăng nhập và bị yêu cầu OTP)
    // =========================================================================

    // Hàm này được gọi từ NavHost khi nhận được pendingToken từ SignInScreen
    fun initLoginMode(pendingToken: String) {
        _state.update {
            it.copy(
                mode = TwoFaMode.LOGIN,
                pendingToken = pendingToken,
                error = null
            )
        }
    }

    // Xác thực OTP để lấy Token đăng nhập (Access Token)
    private suspend fun verifyLoginOtp(code: String): Int {
        var resultStatus = 0
        _state.update { it.copy(isLoading = true, error = null) }

        try {
            val pendingToken = _state.value.pendingToken
            // Gọi API: /auth/login-challenge
            val response = authRepository.loginChallenge(code = code, pendingToken = pendingToken)

            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!

                // --- QUAN TRỌNG: LƯU TOKEN VÀO DATASTORE ---
                userPreferencesRepository.saveAccessToken(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken
                )
                // Lưu thêm thông tin user nếu có, hoặc fetch /me sau khi vào Home
                // -------------------------------------------

                _state.update { it.copy(isLoading = false) }
                resultStatus = 1 // Thành công -> UI chuyển vào Home
            } else {
                _state.update { it.copy(isLoading = false, error = "Mã OTP sai hoặc đã hết hạn") }
                resultStatus = 0
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
            resultStatus = 0
        }
        return resultStatus
    }

    // =========================================================================
    // HELPER & FACTORY
    // =========================================================================

    // Hàm gọi chung từ UI (Nút Xác nhận)
    suspend fun verifyOtp(code: String): Int {
        return if (_state.value.mode == TwoFaMode.SETUP) {
            verifySetupOtp(code)
        } else {
            verifyLoginOtp(code)
        }
    }

    // Hàm hỗ trợ UI (Set key giả lập hoặc từ nav arg nếu cần)
    fun setSecretKey(key: String) {
        _state.update { it.copy(secretKey = key) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MAMApplication)

                // Lấy các Repository từ Application -> BaseRepository
                val twoFaRepository = application.baseRepository.twoFaRepository
                val authRepository = application.baseRepository.authPublicRepository
                val userPreferencesRepository = application.userPreferencesRepository

                TwoFaViewModel(twoFaRepository, authRepository, userPreferencesRepository)
            }
        }
    }
}