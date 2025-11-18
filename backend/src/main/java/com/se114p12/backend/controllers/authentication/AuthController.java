package com.se114p12.backend.controllers.authentication;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.authentication.*;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.services.authentication.AuthService;
import com.se114p12.backend.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth Module", description = "APIs for authentication")
@Controller
@RequestMapping(AppConstant.API_BASE_PATH + "/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;

  @Operation(summary = "Register a new user")
  @ApiResponse(
      responseCode = "201",
      description = "User registered successfully",
      content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
  @ErrorResponse
  @PostMapping("/register")
  @ResponseBody
  public ResponseEntity<UserResponseDTO> register(
      @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(registerRequestDTO));
  }

  @Operation(
      summary = "Login with Google OAuth2",
      description =
          "Login using Google OAuth2 credentials. The Google ID token is sent in the request body.")
  @ErrorResponse
  @PostMapping("/oauth2/login/google")
  @ResponseBody
  public ResponseEntity<AuthResponseDTO> loginWithGoogle(
      @Valid @RequestBody GoogleLoginRequestDTO googleLoginRequestDTO) {
    return ResponseEntity.ok(authService.loginWithGoogle(googleLoginRequestDTO));
  }

  @Operation(
      summary = "Login with Firebase",
      description =
          "Login using Firebase credentials. The Firebase ID token is sent in the request body.")
  @ErrorResponse
  @PostMapping("/oauth2/login/firebase")
  @ResponseBody
  public ResponseEntity<AuthResponseDTO> loginWithFirebase(
      @Valid @RequestBody FirebaseLoginRequestDTO firebaseLoginRequestDTO) {
    return ResponseEntity.ok(authService.loginWithFirebase(firebaseLoginRequestDTO));
  }

  @Operation(
      summary = "Register with Google OAuth2",
      description =
          "Register using Google OAuth2 credentials. The Google ID token is sent in the request"
              + " body.")
  @ErrorResponse
  @PostMapping("/oauth2/register/google")
  @ResponseBody
  public ResponseEntity<UserResponseDTO> registerWithGoogle(
      @Valid @RequestBody GoogleRegisterRequestDTO googleRegisterRequestDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.registerGoogleUser(googleRegisterRequestDTO));
  }

  @Operation(
      summary = "Register with Firebase",
      description =
          "Register using Firebase credentials. The Firebase ID token is sent in the request body.")
  @ErrorResponse
  @PostMapping("/oauth2/register/firebase")
  @ResponseBody
  public ResponseEntity<UserResponseDTO> registerWithFirebase(
      @Valid @RequestBody FirebaseRegisterRequestDTO firebaseRegisterRequestDTO) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.registerFirebaseUser(firebaseRegisterRequestDTO));
  }

  @Operation(
      summary = "Login with credentialId and password",
      description = "credentialId is usually the email or phone number or username")
  @ApiResponse(
      responseCode = "200",
      description = "User logged in successfully",
      content = @Content(schema = @Schema(implementation = AuthResponseDTO.class)))
  @ErrorResponse
  @PostMapping("/login")
  @ResponseBody
  public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
    Object loginResponseDTO = authService.login(loginRequestDTO);
    return ResponseEntity.ok().body(loginResponseDTO);
  }

  @Operation(summary = "Get current user information")
  @ApiResponse(
      responseCode = "200",
      description = "Current user information retrieved successfully",
      content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
  @ErrorResponse
  @GetMapping("/me")
  @ResponseBody
  public ResponseEntity<UserResponseDTO> getCurrentUser() {
    return ResponseEntity.ok().body(userService.getCurrentUser());
  }

  @Operation(summary = "Refresh access token using refresh token")
  @ApiResponse(
      responseCode = "200",
      description = "Access token refreshed successfully",
      content = @Content(schema = @Schema(implementation = AuthResponseDTO.class)))
  @ErrorResponse
  @PostMapping("/refresh")
  @ResponseBody
  public ResponseEntity<AuthResponseDTO> refreshToken(
      @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
    AuthResponseDTO loginResponseDTO = authService.refreshToken(refreshTokenRequestDTO);
    return ResponseEntity.ok().body(loginResponseDTO);
  }

  @Operation(summary = "Log out user")
  @ApiResponse(
      responseCode = "200",
      description = "User logged out successfully",
      content = @Content(schema = @Schema(implementation = String.class)))
  @ErrorResponse
  @PostMapping("/logout")
  @ResponseBody
  public ResponseEntity<String> logout(
      @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
    authService.logout(refreshTokenRequestDTO);
    return ResponseEntity.ok().body("Logout successfully");
  }

  @Operation(summary = "Resend verification email")
  @PostMapping("/send-verify-email")
  public ResponseEntity<Void> sendVerifyEmail(
      @Valid @RequestBody SendVerifyEmailRequestDTO sendVerifyEmailRequestDTO) {
    authService.sendVerificationEmail(sendVerifyEmailRequestDTO);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Verification email")
  @GetMapping("/verify-email")
  public String verifyEmail(@RequestParam(value = "code") String code) {
    authService.verifyEmail(code);
    return "verify-email-success";
  }

  @Operation(summary = "Change user password")
  @ApiResponse(
      responseCode = "200",
      description = "Password changed successfully",
      content = @Content(schema = @Schema(implementation = String.class)))
  @ErrorResponse
  @PostMapping("/change-password")
  @ResponseBody
  public ResponseEntity<String> changePassword(
      @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
    return ResponseEntity.ok().body("Reset password successfully");
  }

  @PostMapping("/send-otp")
  @ResponseBody
  public ResponseEntity<Void> sendOTP(@Valid @RequestBody SendOTPRequestDTO sendOTPRequestDTO) {
    authService.sendOtp(sendOTPRequestDTO);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verify-otp")
  @ResponseBody
  public ResponseEntity<VerifyOTPResponseDTO> verifyOTP(
      @Valid @RequestBody VerifyOTPRequestDTO verifyOTPRequestDTO) {
    String responseCode = authService.verifyOtp(verifyOTPRequestDTO);
    VerifyOTPResponseDTO verifyOTPResponseDTO = new VerifyOTPResponseDTO();
    verifyOTPResponseDTO.setCode(responseCode);
    return ResponseEntity.ok(verifyOTPResponseDTO);
  }

  @PostMapping("/forgot-password")
  @ResponseBody
  public ResponseEntity<String> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
    authService.forgotPassword(forgotPasswordRequestDTO);
    return ResponseEntity.ok().body("Send reset password email successfully");
  }

  @PostMapping("/login-challenge")
  @ResponseBody
  public ResponseEntity<AuthResponseDTO> loginWith2FAChallenge(
      @RequestParam("code") String code, @RequestParam("pendingToken") String pending) {

    return ResponseEntity.ok(authService.loginChallenge(code, pending));
  }
}
