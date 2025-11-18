package com.se114p12.backend.configs;

import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig implements WebMvcConfigurer {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      CustomAuthenticationEntryPoint authenticationEntryPoint,
      JwtAuthenticationConverter jwtAuthenticationConverter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/login-challenge",
                        "/api/v1/auth/oauth2/register/google",
                        "/api/v1/auth/oauth2/login/google",
                        "/api/v1/auth/oauth2/register/firebase",
                        "/api/v1/auth/oauth2/login/firebase",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/send-otp",
                        "/api/v1/auth/verify-otp",
                        "/api/v1/auth/verify-email",
                        "/api/v1/auth/send-verify-email",
                        "/api/v1/storage/**",
                        "/api/v1/metadata/**",
                        "/api/v1/storage/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/v1/payment/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    .authenticationEntryPoint(authenticationEntryPoint))
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AppInterceptor appInterceptor() {
    return new AppInterceptor(jwtUtil, userRepository);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(appInterceptor())
        .addPathPatterns("/api/v1/**")
        .excludePathPatterns(
            "/api/v1/auth/login",
            "/api/v1/auth/me",
            "/api/v1/auth/oauth2/register/google",
            "/api/v1/auth/oauth2/login/google",
            "/api/v1/auth/oauth2/register/firebase",
            "/api/v1/auth/oauth2/login/firebase",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html");
  }
}
