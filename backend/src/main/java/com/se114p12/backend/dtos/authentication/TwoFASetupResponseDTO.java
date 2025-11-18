package com.se114p12.backend.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFASetupResponseDTO {
  private String secret;
  private String qrCodeUrl;
}
