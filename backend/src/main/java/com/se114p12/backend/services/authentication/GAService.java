package com.se114p12.backend.services.authentication;

public interface GAService {
  public static final String ISSUER = "StoreMe";

  String generateKey();

  boolean isValid(String secret, Integer code);

  String generateQRUrl(String secret, String username);

  String generateQRBase64(String qrCodeText);
}
