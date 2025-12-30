package com.se114p12.backend.controllers.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.authentication.AuthResponseDTO;
import com.se114p12.backend.dtos.authentication.LoginRequestDTO;
import com.se114p12.backend.dtos.authentication.RegisterRequestDTO;
import com.se114p12.backend.dtos.user.UserResponseDTO;
import com.se114p12.backend.services.authentication.AuthService;
import com.se114p12.backend.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        userService = Mockito.mock(UserService.class);
        AuthController controller = new AuthController(authService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        objectMapper = new ObjectMapper();
    }

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/auth";
    }

    @Test
    @DisplayName("POST /auth/register returns 201 with user body")
    void register_returns201() throws Exception {
        // Arrange
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setFullname("John Doe");
        req.setPhone("0912345678");
        req.setEmail("john@example.com");
        req.setUsername("johnd");
        req.setPassword("secret");

        UserResponseDTO user = new UserResponseDTO();
        user.setFullname("John Doe");
        user.setUsername("johnd");
        user.setEmail("john@example.com");
        user.setPhone("0912345678");

        when(userService.register(any(RegisterRequestDTO.class))).thenReturn(user);

        // Act + Assert
        mockMvc.perform(
                    post(basePath() + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("johnd")))
                .andExpect(jsonPath("$.fullname", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.phone", is("0912345678")));
    }

    @Test
    @DisplayName("POST /auth/login returns 200 with auth response")
    void login_returns200() throws Exception {
        // Arrange
        LoginRequestDTO req = new LoginRequestDTO();
        req.setCredentialId("john@example.com");
        req.setPassword("secret");

        UserResponseDTO user = new UserResponseDTO();
        user.setUsername("johnd");

        AuthResponseDTO auth = new AuthResponseDTO();
        auth.setAccessToken("access-token");
        auth.setRefreshToken("refresh-token");
        auth.setUser(user);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(auth);

        // Act + Assert
        mockMvc.perform(
                    post(basePath() + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.user.username", is("johnd")));
    }
}
