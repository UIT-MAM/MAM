package com.example.mam.navigation

enum class AuthenticationScreen {
    Start,
    SignIn,
    ForgetPW,
    Terms,
    SignUp,
    OTP,
    TwoFaSetup,    // Màn 1: Quét mã QR
    TwoFaVerify,   // Màn 2: Nhập mã xác thực
    TwoFaRecovery  // Màn 3: Lưu mã khôi phục
}
enum class HomeScreen {
    HomeSreen,
    Notification,
    Search,
    Terms,
    Details,
    Cart,
    CheckOut,
    Order,
    Profile,
    OrderHistory,
    ChangePassword,
    SelectAddress,
}