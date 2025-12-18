package com.se114p12.backend.services.authentication;

import com.se114p12.backend.dtos.authentication.AuthResponseDTO;
import com.se114p12.backend.dtos.authentication.LoginRequestDTO;
import com.se114p12.backend.dtos.authentication.TwoFAChallenge;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.entities.authentication.RefreshToken;
import com.se114p12.backend.entities.authentication.User2FA;
import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.TwoFAMethod;
import com.se114p12.backend.mappers.user.UserMapper;
import com.se114p12.backend.repositories.authentication.User2FARepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.services.general.MailService;
import com.se114p12.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserMapper userMapper;
    @Mock JwtUtil jwtUtil;
    @Mock RefreshTokenService refreshTokenService;
    @Mock UserRepository userRepository;
    @Mock VerificationService verificationService;
    @Mock MailService mailService;
    @Mock User2FARepository user2FARepository;

    @InjectMocks AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // SecurityContext can be static; ensure a clean context each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_returnsTwoFAChallenge_when2FAEnabled() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCredentialId("user@example.com");
        dto.setPassword("secret");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.getCurrentUserId()).thenReturn(100L);

        User2FA u1 = new User2FA();
        u1.setEnabled(true);
        u1.setMethod(TwoFAMethod.TOTP);
        User2FA u2 = new User2FA();
        u2.setEnabled(true);
        u2.setMethod(TwoFAMethod.TOTP);
        when(user2FARepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<com.se114p12.backend.entities.authentication.User2FA>>any())).thenReturn(List.of(u1, u2));

        Verification verification = new Verification();
        verification.setCode("abc123");
        when(verificationService.createTwoFactorVerification(100L)).thenReturn(verification);

        // Act
        Object result = authService.login(dto);

        // Assert
        assertTrue(result instanceof TwoFAChallenge, "Expected TwoFAChallenge when 2FA is enabled");
        TwoFAChallenge challenge = (TwoFAChallenge) result;
        assertEquals(List.of(TwoFAMethod.TOTP, TwoFAMethod.TOTP), challenge.getMethods());
        assertEquals("abc123", challenge.getToken());

        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(refreshTokenService, never()).generateRefreshToken(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(userMapper, never()).entityToResponse(any());
    }

    @Test
    void login_returnsAuthResponse_when2FADisabled() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCredentialId("user@example.com");
        dto.setPassword("secret");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.getCurrentUserId()).thenReturn(42L);

        when(user2FARepository.findAll(ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<com.se114p12.backend.entities.authentication.User2FA>>any())).thenReturn(Collections.emptyList());

        when(jwtUtil.generateAccessToken(42L)).thenReturn("access-token");
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token");
        when(refreshTokenService.generateRefreshToken(42L)).thenReturn(rt);

        User user = new User();
        user.setId(42L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        UserResponseDTO userResp = new UserResponseDTO();
        when(userMapper.entityToResponse(user)).thenReturn(userResp);

        // Act
        Object result = authService.login(dto);

        // Assert
        assertTrue(result instanceof AuthResponseDTO, "Expected AuthResponseDTO when 2FA is disabled");
        AuthResponseDTO auth = (AuthResponseDTO) result;
        assertEquals("access-token", auth.getAccessToken());
        assertEquals("refresh-token", auth.getRefreshToken());
        assertSame(userResp, auth.getUser());

        verify(jwtUtil).generateAccessToken(42L);
        verify(refreshTokenService).generateRefreshToken(42L);
        verify(userRepository).findById(42L);
        verify(userMapper).entityToResponse(user);
    }
}
