package com.example.boardapi.security;

import com.example.boardapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;
    @Override public UserDetails loadUserByUsername(String email) {
        return memberRepository.findByEmail(email).map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."));
    }
}
