package com.example.boardapi.api;

import com.example.boardapi.api.dto.ApiDtos.MemberResponse;
import com.example.boardapi.api.dto.ApiDtos.SessionResponse;
import com.example.boardapi.api.dto.LoginRequest;
import com.example.boardapi.api.dto.SignupRequest;
import com.example.boardapi.domain.Member;
import com.example.boardapi.security.UserPrincipal;
import com.example.boardapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        Member member = authService.signup(request);
        return ResponseEntity.status(201).body(MemberResponse.from(member));
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim().toLowerCase(), request.password()));
        if (httpRequest.getSession(false) != null) httpRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, httpRequest, httpResponse);
        UserPrincipal member = (UserPrincipal) authentication.getPrincipal();
        return new SessionResponse(member.id(), member.email(), member.nickname(), "로그인되었습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        contextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
        return ResponseEntity.noContent().build();
    }
}
