package com.se114p12.backend.services.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.se114p12.backend.dtos.authentication.AuthResponseDTO;
import com.se114p12.backend.dtos.authentication.FirebaseLoginRequestDTO;
import com.se114p12.backend.dtos.authentication.ForgotPasswordRequestDTO;
import com.se114p12.backend.dtos.authentication.GoogleLoginRequestDTO;
import com.se114p12.backend.dtos.authentication.LoginRequestDTO;
import com.se114p12.backend.dtos.authentication.PasswordChangeDTO;
import com.se114p12.backend.dtos.authentication.RefreshTokenRequestDTO;
import com.se114p12.backend.dtos.authentication.SendOTPRequestDTO;
import com.se114p12.backend.dtos.authentication.SendVerifyEmailRequestDTO;
import com.se114p12.backend.dtos.authentication.TwoFAChallenge;
import com.se114p12.backend.dtos.authentication.VerifyOTPRequestDTO;
import com.se114p12.backend.entities.authentication.RefreshToken;
import com.se114p12.backend.entities.authentication.User2FA;
import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.TwoFAMethod;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.enums.VerificationType;
import com.se114p12.backend.exceptions.BadRequestException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.user.UserMapper;
import com.se114p12.backend.repositories.authentication.User2FARepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.services.general.MailService;
import com.se114p12.backend.util.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final UserMapper userMapper;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final UserRepository userRepository;
  private final VerificationService verificationService;
  private final MailService mailService;
  private final User2FARepository user2FARepository;

  @Override
  public Object login(LoginRequestDTO loginRequestDTO) {

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequestDTO.getCredentialId(), loginRequestDTO.getPassword());

    Authentication authentication =
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    Long userId = jwtUtil.getCurrentUserId();

    // Check if user has 2FA enabled
    List<User2FA> user2FAs =
        user2FARepository.findAll(
            (root, _, builder) ->
                builder.and(
                    builder.equal(root.get("user").get("id"), userId),
                    builder.isTrue(root.get("isEnabled"))));
    if (!user2FAs.isEmpty()) {
      List<TwoFAMethod> methods = user2FAs.stream().map(User2FA::getMethod).toList();
      String token = verificationService.createTwoFactorVerification(userId).getCode();
      TwoFAChallenge twoFAChallenge = new TwoFAChallenge();
      twoFAChallenge.setMethods(methods);
      twoFAChallenge.setToken(token);
      return twoFAChallenge;
    }

    String accessToken = jwtUtil.generateAccessToken(userId);

    String refreshToken = refreshTokenService.generateRefreshToken(userId).getToken();

    AuthResponseDTO loginResponseDTO = new AuthResponseDTO();
    loginResponseDTO.setAccessToken(accessToken);
    loginResponseDTO.setRefreshToken(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    loginResponseDTO.setUser(userMapper.entityToResponse(user));

    return loginResponseDTO;
  }

  @Override
  public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO googleLoginRequestDTO) {
    GoogleIdToken googleIdToken =
        jwtUtil.verifyGoogleCredential(
            googleLoginRequestDTO.getCredential(), googleLoginRequestDTO.getClientId());
    GoogleIdToken.Payload payload = googleIdToken.getPayload();

    String email = payload.getEmail();

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    //     create access token
    String accessToken = jwtUtil.generateAccessToken(user.getId());

    // create refresh token
    String refreshToken = refreshTokenService.generateRefreshToken(user.getId()).getToken();

    AuthResponseDTO authenticationResponseDTO = new AuthResponseDTO();
    authenticationResponseDTO.setAccessToken(accessToken);
    authenticationResponseDTO.setRefreshToken(refreshToken);
    return authenticationResponseDTO;
  }

  @Override
  public AuthResponseDTO loginWithFirebase(FirebaseLoginRequestDTO firebaseLoginRequestDTO) {
    FirebaseToken decodedToken;
    try {
      decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseLoginRequestDTO.getIdToken());
    } catch (FirebaseAuthException e) {
      throw new BadRequestException("Invalid Firebase ID token");
    }

    String email = decodedToken.getEmail();

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    //     create access token
    String accessToken = jwtUtil.generateAccessToken(user.getId());

    // create refresh token
    String refreshToken = refreshTokenService.generateRefreshToken(user.getId()).getToken();

    AuthResponseDTO authenticationResponseDTO = new AuthResponseDTO();
    authenticationResponseDTO.setAccessToken(accessToken);
    authenticationResponseDTO.setRefreshToken(refreshToken);
    authenticationResponseDTO.setUser(userMapper.entityToResponse(user));
    return authenticationResponseDTO;
  }

  @Override
  public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
    RefreshToken refreshToken =
        refreshTokenService.verifyExpiration(
            refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken()));

    String accessToken = jwtUtil.generateAccessToken(refreshToken.getUser().getId());

    AuthResponseDTO loginResponseDTO = new AuthResponseDTO();
    loginResponseDTO.setAccessToken(accessToken);
    loginResponseDTO.setRefreshToken(refreshTokenRequestDTO.getRefreshToken());
    loginResponseDTO.setUser(userMapper.entityToResponse(refreshToken.getUser()));
    return loginResponseDTO;
  }

  @Override
  public void logout(RefreshTokenRequestDTO refreshTokenRequestDTO) {
    refreshTokenService.deleteByToken(refreshTokenRequestDTO.getRefreshToken());
  }

  @Override
  public void verifyEmail(String code) {
    Verification verification =
        verificationService.verifyVerificationCode(code, VerificationType.ACTIVATION);
    User user = verification.getUser();
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
    verificationService.deleteVerification(verification);
  }

  @Override
  public void resetPassword(PasswordChangeDTO passwordChangeDTO) {
    Long userId = jwtUtil.getCurrentUserId();
    User currentUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!passwordEncoder.matches(
        passwordChangeDTO.getCurrentPassword(), currentUser.getPassword())) {
      throw new BadRequestException("Password doesn't match");
    }
    currentUser.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
  }

  @Override
  public void sendOtp(SendOTPRequestDTO sendOTPRequestDTO) {
    User user =
        userRepository
            .findByEmail(sendOTPRequestDTO.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    String otp =
        verificationService.createOtpVerification(user.getId(), sendOTPRequestDTO.getAction());
    mailService.sendOtpEmail(user.getEmail(), otp);
  }

  @Override
  public String verifyOtp(VerifyOTPRequestDTO verifyOTPRequestDTO) {
    String code = verifyOTPRequestDTO.getOtp() + "_" + verifyOTPRequestDTO.getAction();
    Verification verification =
        verificationService.verifyVerificationCode(code, VerificationType.OTP);
    verificationService.deleteVerification(verification);
    switch (verifyOTPRequestDTO.getAction()) {
      case FORGOT_PASSWORD:
        Verification resetPasswordVerification =
            verificationService.createResetPasswordVerification(verification.getUser().getId());
        return resetPasswordVerification.getCode();
      default:
        throw new BadRequestException("Invalid action for OTP verification");
    }
  }

  @Override
  public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
    Verification verification =
        verificationService.verifyVerificationCode(
            forgotPasswordRequestDTO.getCode(), VerificationType.RESET_PASSWORD);
    User user = verification.getUser();
    user.setPassword(passwordEncoder.encode(forgotPasswordRequestDTO.getNewPassword()));
    userRepository.save(user);
  }

  @Override
  public void sendVerificationEmail(SendVerifyEmailRequestDTO sendVerifyEmailRequestDTO) {
    User user =
        userRepository
            .findByEmail(sendVerifyEmailRequestDTO.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getStatus() == UserStatus.ACTIVE) {
      throw new BadRequestException("User is already active");
    }
    Verification verification = verificationService.createActivationVerification(user.getId());
    mailService.sendActivationEmail(user.getEmail(), verification.getCode());
  }

  @Override
  public AuthResponseDTO loginChallenge(String code, String pendingToken) {
    Verification verification =
        verificationService.verifyVerificationCode(pendingToken, VerificationType.TWO_FACTOR);

    Long userId = verification.getUser().getId();

    String accessToken = jwtUtil.generateAccessToken(userId);

    String refreshToken = refreshTokenService.generateRefreshToken(userId).getToken();

    AuthResponseDTO loginResponseDTO = new AuthResponseDTO();
    loginResponseDTO.setAccessToken(accessToken);
    loginResponseDTO.setRefreshToken(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    loginResponseDTO.setUser(userMapper.entityToResponse(user));

    verificationService.deleteVerification(verification);

    return loginResponseDTO;
  }
}
