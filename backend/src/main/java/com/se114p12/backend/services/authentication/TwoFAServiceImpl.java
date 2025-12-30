package com.se114p12.backend.services.authentication;

import com.se114p12.backend.constants.RedisConstant;
import com.se114p12.backend.dtos.authentication.TwoFASetupResponseDTO;
import com.se114p12.backend.dtos.authentication.VerifySetup2FAResponseDTO;
import com.se114p12.backend.entities.authentication.User2FA;
import com.se114p12.backend.entities.authentication.User2FABackupCode;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.TwoFAMethod;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.repositories.authentication.User2FABackupCodeRepository;
import com.se114p12.backend.repositories.authentication.User2FARepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.util.JwtUtil;
import com.se114p12.backend.util.OtpGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TwoFAServiceImpl implements TwoFAService {

  private final GAService gaService;
  private final UserRepository userRepository;
  private final User2FARepository user2FARepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final JwtUtil jwtUtil;
  private final User2FABackupCodeRepository user2FABackupCodeRepository;

  @Override
  public TwoFASetupResponseDTO setup2FA(TwoFAMethod method) {
    Long userId = jwtUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    switch (method) {
      case TOTP:
        String secretKey = gaService.generateKey();
        redisTemplate
            .opsForValue()
            .set(RedisConstant.TWO_FA_PREFIX + userId, secretKey, 5, TimeUnit.MINUTES);
        String qrCodeUrl = gaService.generateQRUrl(secretKey, user.getUsername());

        TwoFASetupResponseDTO totpResponseDTO = new TwoFASetupResponseDTO();
        totpResponseDTO.setSecret(secretKey);
        totpResponseDTO.setQrCodeUrl(qrCodeUrl);
        return totpResponseDTO;
      default:
        throw new UnsupportedOperationException("Unimplemented method 'setup2FA'");
    }
  }

  @Override
  public VerifySetup2FAResponseDTO verifySetupTOTP(String code, TwoFAMethod method) {
    Long userId = jwtUtil.getCurrentUserId();
    // Remove any existing 2FA setup for the user and method
    user2FARepository.delete(
        (root, _, builder) ->
            builder.and(
                builder.equal(root.get("user").get("id"), userId),
                builder.equal(root.get("method"), method)));

    User2FA user2FA = null;
    switch (method) {
      case TOTP:
        String secret =
            (String) redisTemplate.opsForValue().get(RedisConstant.TWO_FA_PREFIX + userId);
        if (secret == null) {
          throw new ResourceNotFoundException("TOTP setup not found or expired");
        }
        Integer codeInt = Integer.parseInt(code);
        boolean isValid = gaService.isValid(secret, codeInt);
        if (!isValid) {
          throw new ResourceNotFoundException("Invalid TOTP code");
        }
        user2FA = new User2FA();
        user2FA.setUser(userRepository.getReferenceById(userId));
        user2FA.setMethod(TwoFAMethod.TOTP);
        user2FA.setSecretKey(secret);
        user2FA.setEnabled(true);
        user2FA = user2FARepository.save(user2FA);
        break;
      default:
        throw new UnsupportedOperationException("Unimplemented method 'verifySetupTOTP'");
    }
    // generate backup codes
    VerifySetup2FAResponseDTO responseDTO = new VerifySetup2FAResponseDTO();
    List<String> backupCodes = generateBackupCodes(user2FA);
    responseDTO.setRecoveryCodes(backupCodes);
    return responseDTO;
  }

  @Transactional
  private List<String> generateBackupCodes(User2FA user2FA) {
    List<String> backupCodes = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      String code = OtpGenerator.generateOtp();
      User2FABackupCode backupCode = new User2FABackupCode();
      backupCode.setBackupCode(code);
      backupCode.setUser2FA(user2FA);
      user2FABackupCodeRepository.save(backupCode);
      backupCodes.add(code);
    }
    return backupCodes;
  }
}
