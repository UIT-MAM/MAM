package com.se114p12.backend.services.authentication;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GAServiceImpl implements GAService {
  @Override
  public String generateKey() {
    GoogleAuthenticator gAuth = new GoogleAuthenticator();
    final GoogleAuthenticatorKey key = gAuth.createCredentials();
    return key.getKey();
  }

  @Override
  public boolean isValid(String secret, Integer code) {
    GoogleAuthenticator gAuth =
        new GoogleAuthenticator(
            new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build());
    return gAuth.authorize(secret, code);
  }

  @Override
  public String generateQRUrl(String secret, String username) {
    String url =
        GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            ISSUER, username, new GoogleAuthenticatorKey.Builder(secret).build());
    try {
      return generateQRBase64(url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String generateQRBase64(String qrCodeText) {
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      Map<EncodeHintType, Object> hints = new HashMap<>();
      hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      BitMatrix bitMatrix =
          qrCodeWriter.encode(qrCodeText, com.google.zxing.BarcodeFormat.QR_CODE, 200, 200, hints);
      BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", baos);
      byte[] imageBytes = baos.toByteArray();
      baos.close();
      return Base64.getEncoder().encodeToString(imageBytes);
    } catch (WriterException | IOException e) {
      throw new RuntimeException("Failed to generate QR code", e);
    }
  }
}
