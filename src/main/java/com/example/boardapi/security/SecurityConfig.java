package com.example.boardapi.security;

import com.example.boardapi.api.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .securityContext(context -> context.securityContextRepository(contextRepository))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/h2-console/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/**", "/api/posts", "/api/comments/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.", request.getRequestURI()))
                        .accessDeniedHandler((request, response, exception) -> writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "요청을 수행할 권한이 없습니다.", request.getRequestURI())))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(status.value(), code, message, path));
    }
}
