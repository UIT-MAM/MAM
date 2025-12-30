package com.se114p12.backend.entities.authentication;

import com.se114p12.backend.entities.BaseEntity;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.TwoFAMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_2fa")
public class User2FA extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String secretKey;

  private boolean isEnabled;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TwoFAMethod method;
}
