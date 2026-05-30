package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "PasswordHash", length = 500)
    private String passwordHash;

    @Column(name = "Role", length = 50)
    private String Role = "PLAYER";

    @Column(name = "AuthProvider", length = 50)
    private String authProvider = "LOCAL";

    @Column(name = "ProviderId", length = 255)
    private String providerId;

    @Column(name = "EmailVerified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name ="EmailVerificationToken",unique = true)
    private String emailVerificationToken;

    @Column(name = "EmailVerificationExpiry")
    private LocalDateTime emailVerificationExpiry;

    @Column(name = "IsBanned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Player> players;
}