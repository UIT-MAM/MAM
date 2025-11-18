package com.se114p12.backend.services.authentication;

import com.se114p12.backend.dtos.authentication.AuthResponseDTO;
import com.se114p12.backend.dtos.authentication.FirebaseLoginRequestDTO;
import com.se114p12.backend.dtos.authentication.ForgotPasswordRequestDTO;
import com.se114p12.backend.dtos.authentication.GoogleLoginRequestDTO;
import com.se114p12.backend.dtos.authentication.LoginRequestDTO;
import com.se114p12.backend.dtos.authentication.PasswordChangeDTO;
import com.se114p12.backend.dtos.authentication.RefreshTokenRequestDTO;
import com.se114p12.backend.dtos.authentication.SendOTPRequestDTO;
import com.se114p12.backend.dtos.authentication.SendVerifyEmailRequestDTO;
import com.se114p12.backend.dtos.authentication.VerifyOTPRequestDTO;

public interface AuthService {

  Object login(LoginRequestDTO loginRequestDTO);

  AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO googleLoginRequestDTO);

  AuthResponseDTO loginWithFirebase(FirebaseLoginRequestDTO firebaseLoginRequestDTO);

  AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO);

  void logout(RefreshTokenRequestDTO refreshTokenRequestDTO);

  void sendVerificationEmail(SendVerifyEmailRequestDTO sendVerifyEmailRequestDTO);

  void verifyEmail(String code);

  void resetPassword(PasswordChangeDTO passwordChangeDTO);

  void sendOtp(SendOTPRequestDTO sendOTPRequestDTO);

  String verifyOtp(VerifyOTPRequestDTO verifyOTPRequestDTO);

  void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO);

  AuthResponseDTO loginChallenge(String code, String pendingToken);
}
