package com.se114p12.backend.entities.authentication;

import com.se114p12.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_2fa_backup_code")
public class User2FABackupCode extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_2fa_id", nullable = false)
  private User2FA user2FA;

  @Column(nullable = false, unique = true)
  private String backupCode;

  @Column(nullable = false)
  private boolean isUsed;
}
