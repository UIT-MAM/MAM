package com.se114p12.backend.dtos.authentication;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifySetup2FAResponseDTO {
  private List<String> recoveryCodes;
}
