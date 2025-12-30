package com.se114p12.backend.services.authentication;

import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.enums.OTPAction;
import com.se114p12.backend.enums.VerificationType;

public interface VerificationService {
  Verification createActivationVerification(Long userId);

  Verification createResetPasswordVerification(Long userId);

  String createOtpVerification(Long userId, OTPAction action);

  Verification createTwoFactorVerification(Long userId);

  Verification verifyVerificationCode(String code, VerificationType type);

  void deleteVerification(Verification verification);

  void deleteExpiredVerifications();
}
