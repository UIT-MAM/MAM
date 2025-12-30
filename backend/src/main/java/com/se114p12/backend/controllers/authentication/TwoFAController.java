package com.se114p12.backend.controllers.authentication;

import com.se114p12.backend.dtos.authentication.TwoFASetupResponseDTO;
import com.se114p12.backend.dtos.authentication.VerifySetup2FAResponseDTO;
import com.se114p12.backend.enums.TwoFAMethod;
import com.se114p12.backend.services.authentication.TwoFAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "2FA Module", description = "Quản lý xác thực hai yếu tố")
@RequestMapping("/2fa")
@RequiredArgsConstructor
@RestController
public class TwoFAController {

  private final TwoFAService twoFAService;

  @Operation(summary = "Thiết lập xác thực hai yếu tố")
  @PostMapping("/setup-totp")
  public ResponseEntity<TwoFASetupResponseDTO> enableTwoFactorAuthentication(
      @RequestParam("method") TwoFAMethod method) {
    return ResponseEntity.ok(twoFAService.setup2FA(method));
  }

  @Operation(summary = "Xác nhận xác thực hai yếu tố")
  @PostMapping("/confirm-setup-totp")
  public ResponseEntity<VerifySetup2FAResponseDTO> confirmTwoFactorAuthentication(
      @RequestParam("code") String code, @RequestParam("method") TwoFAMethod method) {
    return ResponseEntity.ok(twoFAService.verifySetupTOTP(code, method));
  }
}
