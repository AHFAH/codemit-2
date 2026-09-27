package com.example.boardapi.service;

import com.example.boardapi.api.dto.SignupRequest;
import com.example.boardapi.api.error.ApiException;
import com.example.boardapi.domain.Member;
import com.example.boardapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (memberRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }
        return memberRepository.save(new Member(email, passwordEncoder.encode(request.password()), request.nickname().trim()));
    }
}
