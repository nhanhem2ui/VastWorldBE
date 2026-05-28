package com.vastworld.vwbe.services;

import com.vastworld.vwbe.common.RoleConstants;
import com.vastworld.vwbe.dto.auth.*;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Long jwtExpiration;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, @Value("${jwt.expiration}") Long jwtExpiration) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtExpiration = jwtExpiration;
    }

    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        if (accountRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setEmail(request.email());
        account.setUsername(request.username());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setAuthProvider("LOCAL");
        account.setEmailVerified(false);
        account.setIsBanned(false);
        account.setRole(RoleConstants.PLAYER.getValue());

        accountRepository.save(account);

        return new AuthResponse(null, 0L, account.getUsername(), account.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(account);
        return new AuthResponse(token, jwtExpiration, account.getUsername(), account.getEmail());
    }
}