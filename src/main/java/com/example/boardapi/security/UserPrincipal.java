package com.example.boardapi.security;

import com.example.boardapi.domain.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(Long id, String email, String passwordHash, String nickname) implements UserDetails {
    public static UserPrincipal from(Member member) {
        return new UserPrincipal(member.getId(), member.getEmail(), member.getPasswordHash(), member.getNickname());
    }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
}
