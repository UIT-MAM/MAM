package com.se114p12.backend.services.authentication;

import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.enums.OTPAction;
import com.se114p12.backend.enums.VerificationType;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.authentication.VerificationRepository;
import com.se114p12.backend.util.OtpGenerator;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
  private static final Long ACTIVATION_EXPIRATION_TIME = 60 * 60L; // 1 hour
  private static final Long RESET_PASSWORD_EXPIRATION_TIME = 15 * 60L; // 15 minutes
  private static final Long OTP_EXPIRATION_TIME = 5 * 60L; // 5 minutes

  private final VerificationRepository verificationRepository;
  private final UserRepository userRepository;

  @Override
  public Verification createActivationVerification(Long userId) {
    return createVerification(
        userId,
        VerificationType.ACTIVATION,
        UUID.randomUUID().toString(),
        ACTIVATION_EXPIRATION_TIME);
  }

  @Override
  public Verification createResetPasswordVerification(Long userId) {
    return createVerification(
        userId,
        VerificationType.RESET_PASSWORD,
        UUID.randomUUID().toString(),
        RESET_PASSWORD_EXPIRATION_TIME);
  }

  @Override
  public Verification createTwoFactorVerification(Long userId) {
    return createVerification(
        userId, VerificationType.TWO_FACTOR, UUID.randomUUID().toString(), OTP_EXPIRATION_TIME);
  }

  @Override
  public String createOtpVerification(Long userId, OTPAction action) {
    String otpCode = OtpGenerator.generateOtp();
    String verificationCode = otpCode + "_" + action.name();
    createVerification(userId, VerificationType.OTP, verificationCode, OTP_EXPIRATION_TIME);
    return otpCode;
  }

  @Override
  public Verification verifyVerificationCode(String code, VerificationType type) {
    Verification verification =
        verificationRepository
            .findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));
    if (!verification.getType().equals(type)) {
      throw new ResourceNotFoundException("Verification type mismatch");
    }
    if (verification.getExpiredAt().isBefore(Instant.now())) {
      verificationRepository.delete(verification);
      throw new ResourceNotFoundException("Verification code expired");
    }
    return verification;
  }

  @Override
  public void deleteVerification(Verification verification) {
    verificationRepository.delete(verification);
  }

  /**
   * Scheduled task to delete expired verifications. This method is currently empty and should be
   * implemented to remove expired verifications.
   */
  @Scheduled(cron = "0 0 0 * * ?")
  @Override
  public void deleteExpiredVerifications() {
    verificationRepository.deleteExpiredVerifications();
  }

  /**
   * Creates a verification with the specified type and expiration time.
   *
   * @param userId the ID of the user for whom the verification is created
   * @param type the type of verification
   * @param expirationTime the expiration time in seconds
   * @return the created Verification entity
   */
  private Verification createVerification(
      Long userId, VerificationType type, String code, Long expirationTime) {
    Verification verification = new Verification();
    verification.setUser(
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    verification.setType(type);
    verification.setCode(code);
    verification.setExpiredAt(Instant.now().plusSeconds(expirationTime));
    return verificationRepository.save(verification);
  }
}
