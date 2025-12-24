package com.se114p12.backend.controllers.user;

// Test class for UserController

import com.se114p12.backend.dtos.user.UserRequestDTO;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.services.user.UserService;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

  private UserService userService;
  private UserController controller;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    controller = new UserController(userService);
  }

  private UserResponseDTO sampleResponse(Long id) {
    UserResponseDTO dto = new UserResponseDTO();
    dto.setId(id);
    dto.setFullname("Full Name");
    dto.setUsername("username");
    dto.setEmail("user@example.com");
    dto.setPhone("0123456789");
    dto.setAvatarUrl("/img.png");
    dto.setStatus(UserStatus.ACTIVE);
    dto.setCreatedAt(Instant.now());
    dto.setUpdatedAt(Instant.now());
    return dto;
  }

  private UserRequestDTO sampleRequest() {
    UserRequestDTO dto = new UserRequestDTO();
    dto.setFullname("Full Name");
    dto.setUsername("username");
    dto.setEmail("user@example.com");
    dto.setPhone("0123456789");
    dto.setRoleId(1L);
    return dto;
  }

  @Test
  @DisplayName("getAllUsers - should forward pageable and specification to service (paged)")
  void getAllUsers_paged_forwardToService() {
    Pageable pageable = PageRequest.of(1, 5);
    @SuppressWarnings("unchecked")
    Specification spec = mock(Specification.class);

    PageVO<UserResponseDTO> page = PageVO.<UserResponseDTO>builder()
        .page(1)
        .size(5)
        .totalElements(2L)
        .totalPages(1)
        .numberOfElements(2)
        .content(List.of(sampleResponse(1L), sampleResponse(2L)))
        .build();

    when(userService.getAllUsers(spec, pageable)).thenReturn(page);

    ResponseEntity<PageVO<UserResponseDTO>> resp = controller.getAllUsers(pageable, spec);

    assertEquals(200, resp.getStatusCodeValue());
    assertNotNull(resp.getBody());
    assertEquals(2, resp.getBody().getContent().size());

    verify(userService, times(1)).getAllUsers(spec, pageable);
  }

  @Test
  @DisplayName("getAllUsers - should replace non-paged pageable with unpaged")
  void getAllUsers_unpaged_usesUnpaged() {
    Pageable unpaged = Pageable.unpaged();
    // service should receive Pageable.unpaged()
    PageVO<UserResponseDTO> page = PageVO.<UserResponseDTO>builder()
        .page(0).size(0).totalElements(0L).totalPages(0).numberOfElements(0).content(List.of()).build();

    when(userService.getAllUsers(null, Pageable.unpaged())).thenReturn(page);

    ResponseEntity<PageVO<UserResponseDTO>> resp = controller.getAllUsers(unpaged, null);

    assertEquals(200, resp.getStatusCodeValue());
    assertNotNull(resp.getBody());
    assertEquals(0, resp.getBody().getTotalElements());

    verify(userService, times(1)).getAllUsers(null, Pageable.unpaged());
  }

  @Test
  @DisplayName("getUserById - should return user response from service")
  void getUserById_success() {
    UserResponseDTO dto = sampleResponse(5L);
    when(userService.getUserById(5L)).thenReturn(dto);

    ResponseEntity<UserResponseDTO> resp = controller.getUserById(5L);

    assertEquals(200, resp.getStatusCodeValue());
    assertEquals(5L, resp.getBody().getId());
    verify(userService).getUserById(5L);
  }

  @Test
  @DisplayName("updateUser - should call service update and return response")
  void updateUser_callsService() {
    UserRequestDTO req = sampleRequest();
    UserResponseDTO res = sampleResponse(10L);
    when(userService.update(10L, req)).thenReturn(res);

    ResponseEntity<UserResponseDTO> resp = controller.updateUser(10L, req);

    assertEquals(200, resp.getStatusCodeValue());
    assertEquals(10L, resp.getBody().getId());
    verify(userService).update(10L, req);
  }

  @Test
  @DisplayName("deleteUser - should call service delete and return no content")
  void deleteUser_callsService() {
    doNothing().when(userService).delete(7L);

    ResponseEntity<Void> resp = controller.deleteUser(7L);

    assertEquals(204, resp.getStatusCodeValue());
    verify(userService).delete(7L);
  }

  @Test
  @DisplayName("assignRoleToUser - should forward to service and return no content")
  void assignRoleToUser_callsService() {
    doNothing().when(userService).assignRoleToUser(2L, 3L);

    ResponseEntity<Void> resp = controller.assignRoleToUser(2L, 3L);

    assertEquals(204, resp.getStatusCodeValue());
    verify(userService).assignRoleToUser(2L, 3L);
  }

  @Test
  @DisplayName("updateStatus - should parse status and call service")
  void updateStatus_success() {
    doNothing().when(userService).updateUserStatus(4L, UserStatus.ACTIVE);

    ResponseEntity<Void> resp = controller.updateStatus(4L, "ACTIVE");

    assertEquals(204, resp.getStatusCodeValue());
    verify(userService).updateUserStatus(4L, UserStatus.ACTIVE);
  }

  @Test
  @DisplayName("updateStatus - should throw IllegalArgumentException for invalid status")
  void updateStatus_invalid_throws() {
    assertThrows(IllegalArgumentException.class, () -> controller.updateStatus(4L, "NOT_A_STATUS"));
  }
}

