package com.se114p12.backend.services.user;

import com.se114p12.backend.dtos.authentication.RegisterRequestDTO;
import com.se114p12.backend.dtos.user.UserRequestDTO;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.exceptions.DataConflictException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.user.UserMapper;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.services.authentication.VerificationService;
import com.se114p12.backend.services.general.MailService;
import com.se114p12.backend.services.general.SMSService;
import com.se114p12.backend.services.general.StorageService;
import com.se114p12.backend.util.ImageLoader;
import com.se114p12.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock JwtUtil jwtUtil;
    @Mock VerificationService verificationService;
    @Mock MailService mailService;
    @Mock SMSService smsService;
    @Mock StorageService storageService;
    @Mock ImageLoader imageLoader;

    @InjectMocks UserServiceImpl userService;

    RegisterRequestDTO request;

    @BeforeEach
    void setup() {
        request = new RegisterRequestDTO();
        request.setFullname("John Doe");
        request.setUsername("johnd");
        request.setPassword("plainPass");
        request.setEmail("john@example.com");
        request.setPhone("0912345678");
    }

    @Test
    @DisplayName("register(): success creates user, encodes password, formats phone, sends email and returns DTO")
    void register_success() {
        // unique checks
        when(userRepository.existsByUsername("johnd")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        // phone lookup passes
        when(smsService.lookupPhoneNumber("0912345678")).thenReturn(true);
        // role present
        Role role = new Role();
        role.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        // encode password
        when(passwordEncoder.encode("plainPass")).thenReturn("ENCODED");

        // save assigns id
        User saved = new User();
        saved.setId(10L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            // mimic persistence setting id on returned entity
            u.setId(10L);
            return u;
        });

        // verification creation and email
        Verification ver = new Verification();
        ver.setCode("VER123");
        when(verificationService.createActivationVerification(10L)).thenReturn(ver);

        // mapper
        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.entityToResponse(any(User.class))).thenReturn(dto);

        // Act
        UserResponseDTO result = userService.register(request);

        // Assert
        assertSame(dto, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();
        assertEquals("John Doe", persisted.getFullname());
        assertEquals("johnd", persisted.getUsername());
        assertEquals("ENCODED", persisted.getPassword());
        assertEquals("john@example.com", persisted.getEmail());
        assertEquals(SMSService.formatPhoneNumber("0912345678"), persisted.getPhone());
        assertEquals(UserStatus.PENDING, persisted.getStatus());
        assertEquals(LoginProvider.LOCAL, persisted.getLoginProvider());
        assertSame(role, persisted.getRole());

        verify(passwordEncoder).encode("plainPass");
        verify(verificationService).createActivationVerification(10L);
        verify(mailService).sendActivationEmail("john@example.com", "VER123");
        verify(userMapper).entityToResponse(any(User.class));
    }

    @Test
    @DisplayName("register(): username exists -> throws DataConflictException and does not save")
    void register_conflict_username() {
        when(userRepository.existsByUsername("johnd")).thenReturn(true);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(smsService.lookupPhoneNumber("0912345678")).thenReturn(true);

        DataConflictException ex = assertThrows(DataConflictException.class, () -> userService.register(request));
        assertTrue(ex.getErrors().containsKey("username"));

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(anyString(), anyString());
        verify(verificationService, never()).createActivationVerification(anyLong());
    }

    @Test
    @DisplayName("register(): email exists -> throws DataConflictException and does not save")
    void register_conflict_email() {
        when(userRepository.existsByUsername("johnd")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(smsService.lookupPhoneNumber("0912345678")).thenReturn(true);

        DataConflictException ex = assertThrows(DataConflictException.class, () -> userService.register(request));
        assertTrue(ex.getErrors().containsKey("email"));

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(anyString(), anyString());
        verify(verificationService, never()).createActivationVerification(anyLong());
    }

    @Test
    @DisplayName("register(): phone exists -> throws DataConflictException and does not save")
    void register_conflict_phoneExists() {
        when(userRepository.existsByUsername("johnd")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(true);
        when(smsService.lookupPhoneNumber("0912345678")).thenReturn(true);

        DataConflictException ex = assertThrows(DataConflictException.class, () -> userService.register(request));
        assertTrue(ex.getErrors().containsKey("phone"));

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(anyString(), anyString());
        verify(verificationService, never()).createActivationVerification(anyLong());
    }

    @Test
    @DisplayName("register(): invalid phone -> throws DataConflictException and does not save")
    void register_conflict_invalidPhone() {
        when(userRepository.existsByUsername("johnd")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(smsService.lookupPhoneNumber("0912345678")).thenReturn(false);

        DataConflictException ex = assertThrows(DataConflictException.class, () -> userService.register(request));
        assertTrue(ex.getErrors().containsKey("phone"));

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(anyString(), anyString());
        verify(verificationService, never()).createActivationVerification(anyLong());
    }
    
    @Test
    @DisplayName("getAllUsers(): maps page to PageVO with DTO content")
    void getAllUsers_success() {
        // Arrange
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);
        Page<User> page = new PageImpl<>(List.of(u1, u2), PageRequest.of(2, 5), 17);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        UserResponseDTO d1 = new UserResponseDTO();
        UserResponseDTO d2 = new UserResponseDTO();
        when(userMapper.entityToResponse(u1)).thenReturn(d1);
        when(userMapper.entityToResponse(u2)).thenReturn(d2);

        // Act
        var vo = userService.getAllUsers(mock(Specification.class), PageRequest.of(2, 5));

        // Assert
        assertEquals(2, vo.getPage());
        assertEquals(5, vo.getSize());
        assertEquals(17L, vo.getTotalElements());
        assertEquals(page.getTotalPages(), vo.getTotalPages());
        assertEquals(2, vo.getNumberOfElements());
        assertEquals(List.of(d1, d2), vo.getContent());
        verify(userMapper).entityToResponse(u1);
        verify(userMapper).entityToResponse(u2);
    }

    @Test
    @DisplayName("getUserById(): returns DTO when user exists")
    void getUserById_success() {
        User u = new User(); u.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.entityToResponse(u)).thenReturn(dto);

        UserResponseDTO res = userService.getUserById(5L);
        assertSame(dto, res);
    }

    @Test
    @DisplayName("getUserById(): throws when user not found")
    void getUserById_notFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(404L));
    }

    @Test
    @DisplayName("findByPhone(): returns DTO when exists")
    void findByPhone_success() {
        User u = new User(); u.setId(7L);
        when(userRepository.findByPhone("0900000000")).thenReturn(Optional.of(u));
        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.entityToResponse(u)).thenReturn(dto);
        UserResponseDTO res = userService.findByPhone("0900000000");
        assertSame(dto, res);
    }

    @Test
    @DisplayName("findByPhone(): throws when not found")
    void findByPhone_notFound() {
        when(userRepository.findByPhone("0900000000")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findByPhone("0900000000"));
    }

    @Test
    @DisplayName("update(): applies partial update, stores avatar when provided, saves and maps")
    void update_withAvatar() {
        // existing user
        User existing = new User();
        existing.setId(11L);
        existing.setUsername("olduser");
        existing.setEmail("old@example.com");
        existing.setPhone("0901111222");
        when(userRepository.findById(11L)).thenReturn(Optional.of(existing));

        // request with same unique fields to avoid conflict
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("olduser");
        req.setEmail("old@example.com");
        req.setPhone("0901111222");
        // mock avatar present
        MultipartFile avatar = mock(MultipartFile.class);
        when(avatar.isEmpty()).thenReturn(false);
        req.setAvatar(avatar);

        when(storageService.store(eq(avatar), anyString())).thenReturn("/files/u/avatar.png");

        // repository uniqueness checks are not triggered since unique fields unchanged

        // save returns updated user
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.entityToResponse(any(User.class))).thenReturn(dto);

        UserResponseDTO res = userService.update(11L, req);

        assertSame(dto, res);
        verify(userMapper).partialUpdate(req, existing);
        assertEquals("/files/u/avatar.png", existing.getAvatarUrl());
        verify(storageService).store(eq(avatar), anyString());
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("update(): does not store avatar when not provided")
    void update_withoutAvatar() {
        User existing = new User();
        existing.setId(12L);
        existing.setUsername("u");
        existing.setEmail("e@e");
        existing.setPhone("090");
        when(userRepository.findById(12L)).thenReturn(Optional.of(existing));

        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("u"); req.setEmail("e@e"); req.setPhone("090");
        req.setAvatar(null); // not provided

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.entityToResponse(any(User.class))).thenReturn(dto);

        UserResponseDTO res = userService.update(12L, req);
        assertSame(dto, res);
        verify(storageService, never()).store(any(), anyString());
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("delete(): deletes user and avatar file when present")
    void delete_withAvatar() {
        User u = new User();
        u.setId(20L);
        u.setAvatarUrl("/files/ava.png");
        when(userRepository.findById(20L)).thenReturn(Optional.of(u));

        userService.delete(20L);

        verify(userRepository).delete(u);
        verify(storageService).delete("/files/ava.png");
    }

    @Test
    @DisplayName("delete(): deletes user only when avatar missing")
    void delete_withoutAvatar() {
        User u = new User(); u.setId(21L);
        when(userRepository.findById(21L)).thenReturn(Optional.of(u));

        userService.delete(21L);

        verify(userRepository).delete(u);
        verify(storageService, never()).delete(anyString());
    }

    @Test
    @DisplayName("delete(): throws when user not found")
    void delete_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.delete(999L));
    }
}
