package com.se114p12.backend.controllers.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private static final String BASE_URL = AppConstant.API_BASE_PATH + "/users";

    private Role userRole;
    private Role adminRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear existing users (but not roles - they are initialized by DataInitializer)
        userRepository.deleteAll();

        // Get existing roles created by DataInitializer
        userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setDescription("Regular user role");
            role.setActive(true);
            return roleRepository.save(role);
        });

        adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ADMIN");
            role.setDescription("Administrator role");
            role.setActive(true);
            return roleRepository.save(role);
        });

        // Create test user
        testUser = new User();
        testUser.setFullname("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPhone("+84987654321");
        testUser.setPassword("password123");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLoginProvider(LoginProvider.LOCAL);
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);

        // Configure JwtUtil mock to return the test user's ID
        when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    @Nested
    @DisplayName("GET /api/v1/users - Get All Users")
    class GetAllUsersTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return paginated list of users when authenticated")
        void getAllUsers_Authenticated_ReturnsPaginatedList() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(10)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return all users when unpaged")
        void getAllUsers_Unpaged_ReturnsAllUsers() throws Exception {
            // Create additional users
            User user2 = new User();
            user2.setFullname("User Two");
            user2.setUsername("usertwo");
            user2.setEmail("usertwo@example.com");
            user2.setPhone("+84987654322");
            user2.setPassword("password");
            user2.setStatus(UserStatus.ACTIVE);
            user2.setLoginProvider(LoginProvider.LOCAL);
            user2.setRole(userRole);
            userRepository.save(user2);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return filtered users when filter is applied")
        void getAllUsers_WithFilter_ReturnsFilteredUsers() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("filter", "username:'testuser'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].username", is("testuser")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty page when no users match filter")
        void getAllUsers_NoMatch_ReturnsEmptyPage() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("filter", "username:'nonexistent'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return user when found")
        void getUserById_Found_ReturnsUser() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                    .andExpect(jsonPath("$.fullname", is("Test User")))
                    .andExpect(jsonPath("$.username", is("testuser")))
                    .andExpect(jsonPath("$.email", is("testuser@example.com")))
                    .andExpect(jsonPath("$.phone", is("+84987654321")))
                    .andExpect(jsonPath("$.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.role.name", is("USER")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when user not found")
        void getUserById_NotFound_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
            MockMultipartFile requestPart = new MockMultipartFile(
                    "fullname",
                    null,
                    MediaType.TEXT_PLAIN_VALUE,
                    "Updated Name".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .file(requestPart)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should update user successfully")
        void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .param("phone", "0987654323")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullname", is("Updated Name")))
                    .andExpect(jsonPath("$.username", is("updateduser")))
                    .andExpect(jsonPath("$.email", is("updated@example.com")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should update user with avatar")
        void updateUser_WithAvatar_ReturnsUpdatedUser() throws Exception {
            MockMultipartFile avatar = new MockMultipartFile(
                    "avatar",
                    "avatar.png",
                    MediaType.IMAGE_PNG_VALUE,
                    "fake image content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .file(avatar)
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .param("phone", "0987654323")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullname", is("Updated Name")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when updating non-existent user")
        void updateUser_NotFound_Returns404() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/99999")
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .param("phone", "0987654323")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 422 when fullname is blank")
        void updateUser_BlankFullname_ReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when username format is invalid")
        void updateUser_InvalidUsername_ReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "Updated Name")
                            .param("username", "123invalid") // starts with number
                            .param("email", "updated@example.com")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when email format is invalid")
        void updateUser_InvalidEmail_ReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "invalid-email")
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when phone format is invalid")
        void updateUser_InvalidPhone_ReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .param("phone", "123456") // invalid phone format
                            .param("roleId", userRole.getId().toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when roleId is null")
        void updateUser_NullRoleId_ReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testUser.getId())
                            .param("fullname", "Updated Name")
                            .param("username", "updateduser")
                            .param("email", "updated@example.com")
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is(422));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should delete user successfully")
        void deleteUser_ValidId_ReturnsNoContent() throws Exception {
            Long userId = testUser.getId();

            mockMvc.perform(delete(BASE_URL + "/" + userId))
                    .andExpect(status().isNoContent());

            // Verify user is deleted
            assertFalse(userRepository.findById(userId).isPresent());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when deleting non-existent user")
        void deleteUser_NotFound_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{id}/assign-role/{roleId} - Assign Role To User")
    class AssignRoleToUserTests {
        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should assign role to user successfully when admin")
        void assignRole_AsAdmin_ReturnsNoContent() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + testUser.getId() + "/assign-role/" + adminRole.getId()))
                    .andExpect(status().isNoContent());

            // Verify role is assigned
            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals("ADMIN", updatedUser.getRole().getName());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should return 404 when user not found")
        void assignRole_UserNotFound_Returns404() throws Exception {
            mockMvc.perform(post(BASE_URL + "/99999/assign-role/" + adminRole.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should return 404 when role not found")
        void assignRole_RoleNotFound_Returns404() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + testUser.getId() + "/assign-role/99999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/status - Update User Status")
    class UpdateUserStatusTests {
        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return error when user does not have ADMIN role")
        void updateStatus_NonAdmin_ReturnsError() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "INACTIVE"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should update user status to INACTIVE successfully when admin")
        void updateStatus_ToInactive_ReturnsNoContent() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "INACTIVE"))
                    .andExpect(status().isNoContent());

            // Verify status is updated
            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals(UserStatus.INACTIVE, updatedUser.getStatus());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should update user status to BLOCKED successfully when admin")
        void updateStatus_ToBlocked_ReturnsNoContent() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "BLOCKED"))
                    .andExpect(status().isNoContent());

            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals(UserStatus.BLOCKED, updatedUser.getStatus());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should update user status to PENDING successfully when admin")
        void updateStatus_ToPending_ReturnsNoContent() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "PENDING"))
                    .andExpect(status().isNoContent());

            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals(UserStatus.PENDING, updatedUser.getStatus());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should update user status to DELETED successfully when admin")
        void updateStatus_ToDeleted_ReturnsNoContent() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "DELETED"))
                    .andExpect(status().isNoContent());

            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals(UserStatus.DELETED, updatedUser.getStatus());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should return 500 for invalid status value")
        void updateStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status")
                            .param("status", "INVALID_STATUS"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should return 404 when user not found")
        void updateStatus_UserNotFound_Returns404() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/99999/status")
                            .param("status", "INACTIVE"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Should return 400 when status parameter is missing")
        void updateStatus_MissingStatus_ReturnsBadRequest() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/" + testUser.getId() + "/status"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Response DTO Validation")
    class ResponseDtoValidationTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return correct user response structure")
        void getUserById_CorrectResponseStructure() throws Exception {
            MvcResult result = mockMvc.perform(get(BASE_URL + "/" + testUser.getId()))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            UserResponseDTO responseDTO = objectMapper.readValue(responseBody, UserResponseDTO.class);

            assertNotNull(responseDTO.getId());
            assertEquals("Test User", responseDTO.getFullname());
            assertEquals("testuser", responseDTO.getUsername());
            assertEquals("testuser@example.com", responseDTO.getEmail());
            assertEquals("+84987654321", responseDTO.getPhone());
            assertEquals(UserStatus.ACTIVE, responseDTO.getStatus());
            assertNotNull(responseDTO.getCreatedAt());
            assertNotNull(responseDTO.getUpdatedAt());
            assertNotNull(responseDTO.getRole());
            assertEquals("USER", responseDTO.getRole().getName());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return correct page structure for get all users")
        void getAllUsers_CorrectPageStructure() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(5)))
                    .andExpect(jsonPath("$.totalElements", isA(Number.class)))
                    .andExpect(jsonPath("$.totalPages", isA(Number.class)))
                    .andExpect(jsonPath("$.numberOfElements", isA(Number.class)))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Data Integrity")
    class EdgeCasesTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle pagination with large page number")
        void getAllUsers_LargePageNumber_ReturnsEmptyPage() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "1000")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle small page size")
        void getAllUsers_SmallPageSize_ReturnsSingleItem() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.size", is(1)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle sorting by username")
        void getAllUsers_SortByUsername_ReturnsSortedList() throws Exception {
            // Create additional user
            User user2 = new User();
            user2.setFullname("Another User");
            user2.setUsername("anotheruser"); // alphabetically before 'testuser'
            user2.setEmail("another@example.com");
            user2.setPhone("+84987654322");
            user2.setPassword("password");
            user2.setStatus(UserStatus.ACTIVE);
            user2.setLoginProvider(LoginProvider.LOCAL);
            user2.setRole(userRole);
            userRepository.save(user2);

            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "username,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].username", is("anotheruser")))
                    .andExpect(jsonPath("$.content[1].username", is("testuser")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle filter by status")
        void getAllUsers_FilterByStatus_ReturnsFilteredUsers() throws Exception {
            // Create inactive user
            User inactiveUser = new User();
            inactiveUser.setFullname("Inactive User");
            inactiveUser.setUsername("inactiveuser");
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPhone("+84987654324");
            inactiveUser.setPassword("password");
            inactiveUser.setStatus(UserStatus.INACTIVE);
            inactiveUser.setLoginProvider(LoginProvider.LOCAL);
            inactiveUser.setRole(userRole);
            userRepository.save(inactiveUser);

            mockMvc.perform(get(BASE_URL)
                            .param("filter", "status:'ACTIVE'"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
        }
    }
}

