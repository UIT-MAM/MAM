package com.se114p12.backend.dtos.authentication;

import com.se114p12.backend.enums.TwoFAMethod;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFAChallenge {
  private List<TwoFAMethod> methods;
  private String token;
}
