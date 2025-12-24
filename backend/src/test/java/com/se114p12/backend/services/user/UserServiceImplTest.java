package com.se114p12.backend.services.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.authentication.FirebaseRegisterRequestDTO;
import com.se114p12.backend.dtos.authentication.GoogleRegisterRequestDTO;
import com.se114p12.backend.dtos.authentication.RegisterRequestDTO;
import com.se114p12.backend.dtos.user.UserRequestDTO;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.authentication.Verification;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.RoleName;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.exceptions.BadRequestException;
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
import com.se114p12.backend.vo.PageVO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private UserRepository userRepository;
  private RoleRepository roleRepository;
  private JwtUtil jwtUtil;
  private VerificationService verificationService;
  private MailService mailService;
  private SMSService smsService;
  private StorageService storageService;
  private ImageLoader imageLoader;

  private UserServiceImpl service;

  @BeforeEach
  void setUp() {
    userMapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userRepository = mock(UserRepository.class);
    roleRepository = mock(RoleRepository.class);
    jwtUtil = mock(JwtUtil.class);
    verificationService = mock(VerificationService.class);
    mailService = mock(MailService.class);
    smsService = mock(SMSService.class);
    storageService = mock(StorageService.class);
    imageLoader = mock(ImageLoader.class);

    service = new UserServiceImpl(
        userMapper,
        passwordEncoder,
        userRepository,
        roleRepository,
        jwtUtil,
        verificationService,
        mailService,
        smsService,
        storageService,
        imageLoader);
  }

  private User createUser(Long id) {
    User u = new User();
    u.setId(id);
    u.setFullname("Full");
    u.setUsername("user" + id);
    u.setEmail("user" + id + "@example.com");
    u.setPhone("+840123456789");
    u.setStatus(UserStatus.ACTIVE);
    u.setLoginProvider(LoginProvider.LOCAL);
    return u;
  }

  private UserResponseDTO createResponse(Long id) {
    UserResponseDTO dto = new UserResponseDTO();
    dto.setId(id);
    dto.setFullname("Full");
    dto.setUsername("user" + id);
    dto.setEmail("user" + id + "@example.com");
    dto.setPhone("+840123456789");
    dto.setStatus(UserStatus.ACTIVE);
    return dto;
  }

  // ========== getAllUsers ==========
  @Test
  @DisplayName("getAllUsers returns mapped page")
  void getAllUsers_returnsMappedPage() {
    Pageable pageable = PageRequest.of(0, 10);
    User u1 = createUser(1L);
    User u2 = createUser(2L);
    Page<User> page = new PageImpl<>(List.of(u1, u2), pageable, 2);
    Specification<User> spec = (root, query, cb) -> null;
    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(userMapper.entityToResponse(u1)).thenReturn(createResponse(1L));
    when(userMapper.entityToResponse(u2)).thenReturn(createResponse(2L));

    PageVO<UserResponseDTO> result = service.getAllUsers(spec, pageable);

    assertEquals(2, result.getContent().size());
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  // ========== getUserById & findByPhone ==========
  @Test
  @DisplayName("getUserById returns mapped user")
  void getUserById_returnsUser() {
    User u = createUser(5L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(u));
    when(userMapper.entityToResponse(u)).thenReturn(createResponse(5L));

    UserResponseDTO dto = service.getUserById(5L);

    assertEquals(5L, dto.getId());
  }

  @Test
  @DisplayName("getUserById throws when not found")
  void getUserById_throwsNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.getUserById(99L));
  }

  @Test
  @DisplayName("findByPhone returns mapped user")
  void findByPhone_returnsUser() {
    User u = createUser(6L);
    when(userRepository.findByPhone("0123")).thenReturn(Optional.of(u));
    when(userMapper.entityToResponse(u)).thenReturn(createResponse(6L));

    UserResponseDTO dto = service.findByPhone("0123");
    assertEquals(6L, dto.getId());
  }

  @Test
  @DisplayName("findByPhone throws when not found")
  void findByPhone_throws() {
    when(userRepository.findByPhone("000")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.findByPhone("000"));
  }

  // ========== register ==========
  @Test
  @DisplayName("register success creates user and sends verification email")
  void register_success() {
    RegisterRequestDTO req = new RegisterRequestDTO();
    req.setFullname("Full");
    req.setPhone("0123456789");
    req.setEmail("a@b.com");
    req.setUsername("newuser");
    req.setPassword("pwd");

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
    when(userRepository.existsByPhone("0123456789")).thenReturn(false);
    when(smsService.lookupPhoneNumber("0123456789")).thenReturn(true);
    when(passwordEncoder.encode("pwd")).thenReturn("enc");

    Role role = new Role(); role.setId(1L); role.setName("USER");
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

    User saved = createUser(10L);
    when(userRepository.save(any(User.class))).thenReturn(saved);

    Verification v = new Verification(); v.setCode("code");
    when(verificationService.createActivationVerification(10L)).thenReturn(v);

    when(userMapper.entityToResponse(saved)).thenReturn(createResponse(10L));

    UserResponseDTO res = service.register(req);

    assertEquals(10L, res.getId());
    verify(mailService).sendActivationEmail(anyString(), eq("code"));
  }

  @Test
  @DisplayName("register throws DataConflictException when duplicates or invalid phone")
  void register_conflict_throws() {
    RegisterRequestDTO req = new RegisterRequestDTO();
    req.setFullname("Full");
    req.setPhone("0123456789");
    req.setEmail("a@b.com");
    req.setUsername("newuser");
    req.setPassword("pwd");

    when(userRepository.existsByUsername("newuser")).thenReturn(true);
    when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
    when(userRepository.existsByPhone("0123456789")).thenReturn(false);
    when(smsService.lookupPhoneNumber("0123456789")).thenReturn(false);

    assertThrows(DataConflictException.class, () -> service.register(req));
  }

  @Test
  @DisplayName("register throws when role missing")
  void register_roleMissing_throws() {
    RegisterRequestDTO req = new RegisterRequestDTO();
    req.setFullname("Full");
    req.setPhone("0123456789");
    req.setEmail("a@b.com");
    req.setUsername("newuser");
    req.setPassword("pwd");

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
    when(userRepository.existsByPhone("0123456789")).thenReturn(false);
    when(smsService.lookupPhoneNumber("0123456789")).thenReturn(true);
    when(passwordEncoder.encode("pwd")).thenReturn("enc");

    when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.register(req));
  }

  // ========== update ==========
  @Test
  @DisplayName("update saves user, stores avatar if provided")
  void update_savesAndStoresAvatar() {
    UserRequestDTO req = new UserRequestDTO();
    req.setFullname("F");
    req.setUsername("u1");
    req.setEmail("e@e.com");
    req.setPhone("0123");
    req.setRoleId(1L);
    var avatar = mock(org.springframework.web.multipart.MultipartFile.class);
    when(avatar.isEmpty()).thenReturn(false);
    req.setAvatar(avatar);

    User existing = createUser(5L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByPhone(anyString())).thenReturn(false);

    when(storageService.store(avatar, AppConstant.USER_FOLDER)).thenReturn("/uri.png");

    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(userMapper.entityToResponse(any(User.class))).thenReturn(createResponse(5L));

    UserResponseDTO res = service.update(5L, req);
    assertEquals(5L, res.getId());
    verify(storageService).store(avatar, AppConstant.USER_FOLDER);
  }

  @Test
  @DisplayName("update does not store avatar when avatar is empty")
  void update_avatarEmpty_noStore() {
    UserRequestDTO req = new UserRequestDTO();
    req.setFullname("F");
    req.setUsername("u1");
    req.setEmail("e@e.com");
    req.setPhone("0123");
    req.setRoleId(1L);
    var avatar = mock(org.springframework.web.multipart.MultipartFile.class);
    when(avatar.isEmpty()).thenReturn(true);
    req.setAvatar(avatar);

    User existing = createUser(5L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByPhone(anyString())).thenReturn(false);

    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(userMapper.entityToResponse(any(User.class))).thenReturn(createResponse(5L));

    UserResponseDTO res = service.update(5L, req);
    assertEquals(5L, res.getId());
    verify(storageService, never()).store(any(), anyString());
  }

  @Test
  @DisplayName("delete deletes user and removes avatar when present")
  void delete_deletesAndRemovesAvatar() {
    User u = createUser(3L);
    u.setAvatarUrl("/file.png");
    when(userRepository.findById(3L)).thenReturn(Optional.of(u));
    doNothing().when(userRepository).delete(u);
    doNothing().when(storageService).delete("/file.png");

    service.delete(3L);

    verify(userRepository).delete(u);
    verify(storageService).delete("/file.png");
  }

  @Test
  @DisplayName("delete without avatar does not call storage delete")
  void delete_noAvatar() {
    User u = createUser(4L);
    u.setAvatarUrl(null);
    when(userRepository.findById(4L)).thenReturn(Optional.of(u));

    service.delete(4L);

    verify(userRepository).delete(u);
    verify(storageService, never()).delete(anyString());
  }

  // ========== registerGoogleUser ==========
  @Test
  @DisplayName("registerGoogleUser saves new user when email not present")
  void registerGoogleUser_savesNewUser() throws Exception {
    GoogleIdToken token = mock(GoogleIdToken.class);
    GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
    when(token.getPayload()).thenReturn(payload);
    when(payload.getEmail()).thenReturn("g@example.com");
    when(payload.get("name")).thenReturn("G Name");
    when(payload.get("picture")).thenReturn("http://img");

    GoogleRegisterRequestDTO req = new GoogleRegisterRequestDTO();
    req.setCredential("cred");
    req.setClientId("cid");
    req.setPhone("0123456789");

    when(jwtUtil.verifyGoogleCredential("cred", "cid")).thenReturn(token);
    when(userRepository.findByEmail("g@example.com")).thenReturn(Optional.empty());
    when(smsService.lookupPhoneNumber("0123456789")).thenReturn(true);
    when(userRepository.existsByPhone(anyString())).thenReturn(false);
    when(roleRepository.findByName(RoleName.USER.getValue())).thenReturn(Optional.of(new Role()));
    when(imageLoader.saveImageFromUrl("http://img", AppConstant.USER_FOLDER)).thenReturn("/a.png");

    when(userRepository.save(any(User.class))).thenAnswer(i -> {
      User u = i.getArgument(0);
      u.setId(50L);
      return u;
    });
    when(userMapper.entityToResponse(any(User.class))).thenReturn(createResponse(50L));

    UserResponseDTO res = service.registerGoogleUser(req);
    assertEquals(50L, res.getId());
  }

  // ========== registerFirebaseUser ==========
  @Test
  @DisplayName("registerFirebaseUser throws when token invalid")
  void registerFirebaseUser_invalidToken_throws() throws FirebaseAuthException {
    FirebaseRegisterRequestDTO req = new FirebaseRegisterRequestDTO();
    req.setIdToken("bad");
    req.setPhoneNumber("0123");

    try (MockedStatic<FirebaseAuth> mocked = Mockito.mockStatic(FirebaseAuth.class)) {
      FirebaseAuth fa = mock(FirebaseAuth.class);
      mocked.when(FirebaseAuth::getInstance).thenReturn(fa);
      when(fa.verifyIdToken("bad")).thenThrow(new BadRequestException("invalid token"));

      assertThrows(BadRequestException.class, () -> service.registerFirebaseUser(req));
    }
  }

  @Test
  @DisplayName("registerFirebaseUser saves when valid token")
  void registerFirebaseUser_valid_saves() throws FirebaseAuthException {
    FirebaseRegisterRequestDTO req = new FirebaseRegisterRequestDTO();
    req.setIdToken("good");
    req.setPhoneNumber("0123456789");

    FirebaseToken token = mock(FirebaseToken.class);
    when(token.getEmail()).thenReturn("f@example.com");
    when(token.getName()).thenReturn("F Name");
    when(token.getPicture()).thenReturn("http://p");

    try (MockedStatic<FirebaseAuth> mocked = Mockito.mockStatic(FirebaseAuth.class)) {
      FirebaseAuth fa = mock(FirebaseAuth.class);
      mocked.when(FirebaseAuth::getInstance).thenReturn(fa);
      when(fa.verifyIdToken("good")).thenReturn(token);

      when(userRepository.findByEmail("f@example.com")).thenReturn(Optional.empty());
      when(userRepository.existsByPhone(anyString())).thenReturn(false);
      when(smsService.lookupPhoneNumber(anyString())).thenReturn(true);
      when(roleRepository.findByName(RoleName.USER.getValue())).thenReturn(Optional.of(new Role()));
      when(imageLoader.saveImageFromUrl("http://p", AppConstant.USER_FOLDER)).thenReturn("/p.png");
      when(userRepository.existsByUsername(anyString())).thenReturn(false);

      when(userRepository.save(any(User.class))).thenAnswer(i -> {
        User u = i.getArgument(0);
        u.setId(60L);
        return u;
      });
      when(userMapper.entityToResponse(any(User.class))).thenReturn(createResponse(60L));

      UserResponseDTO res = service.registerFirebaseUser(req);
      assertEquals(60L, res.getId());
    }
  }

  // ========== assignRoleToUser ==========
  @Test
  @DisplayName("assignRoleToUser updates role when both exist")
  void assignRoleToUser_success() {
    User u = createUser(2L);
    Role r = new Role(); r.setId(3L);
    when(userRepository.findById(2L)).thenReturn(Optional.of(u));
    when(roleRepository.findById(3L)).thenReturn(Optional.of(r));
    when(userRepository.save(any(User.class))).thenReturn(u);

    service.assignRoleToUser(2L, 3L);
    verify(userRepository).save(u);
  }

  @Test
  @DisplayName("assignRoleToUser throws when user or role missing")
  void assignRoleToUser_missing_throws() {
    when(userRepository.findById(9L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.assignRoleToUser(9L, 1L));

    User u = createUser(8L);
    when(userRepository.findById(8L)).thenReturn(Optional.of(u));
    when(roleRepository.findById(7L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.assignRoleToUser(8L, 7L));
  }

  // ========== updateUserStatus ==========
  @Test
  @DisplayName("updateUserStatus sets status and saves")
  void updateUserStatus_setsAndSaves() {
    User u = createUser(11L);
    when(userRepository.findById(11L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenReturn(u);

    service.updateUserStatus(11L, UserStatus.BLOCKED);
    assertEquals(UserStatus.BLOCKED, u.getStatus());
  }

  // ========== getCurrentUser ==========
  @Test
  @DisplayName("getCurrentUser throws when not authenticated")
  void getCurrentUser_notAuthenticated_throws() {
    when(jwtUtil.getCurrentUserId()).thenReturn(null);
    assertThrows(ResourceNotFoundException.class, () -> service.getCurrentUser());
  }

  @Test
  @DisplayName("getCurrentUser returns user when authenticated")
  void getCurrentUser_returns() {
    when(jwtUtil.getCurrentUserId()).thenReturn(12L);
    User u = createUser(12L);
    when(userRepository.findById(12L)).thenReturn(Optional.of(u));
    when(userMapper.entityToResponse(u)).thenReturn(createResponse(12L));

    UserResponseDTO dto = service.getCurrentUser();
    assertEquals(12L, dto.getId());
  }
}
