package com.example.boardapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 40)
    private String nickname;

    public Member(String email, String passwordHash, String nickname) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
    }
}
