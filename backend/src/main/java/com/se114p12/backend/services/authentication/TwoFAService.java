package com.se114p12.backend.services.authentication;

import com.se114p12.backend.dtos.authentication.TwoFASetupResponseDTO;
import com.se114p12.backend.dtos.authentication.VerifySetup2FAResponseDTO;
import com.se114p12.backend.enums.TwoFAMethod;

public interface TwoFAService {
  TwoFASetupResponseDTO setup2FA(TwoFAMethod method);

  // secret is need when method is TOTP
  VerifySetup2FAResponseDTO verifySetupTOTP(String code, TwoFAMethod method);
}
