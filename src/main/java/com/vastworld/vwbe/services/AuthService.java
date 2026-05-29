package com.vastworld.vwbe.services;

import com.vastworld.vwbe.common.RoleConstants;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.auth.*;
import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    public ServiceResult<AuthResponse> register(RegisterRequest request) {
        try {
            if (accountRepository.existsByEmail(request.email())) {
                return ServiceResult.failure("Email already exists");
            }

            if (accountRepository.existsByUsername(request.username())) {
                return ServiceResult.failure("Username already exists");
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

            return ServiceResult.success("Register Successfully, please check your email");
        } catch (Exception ex) {
            return ServiceResult.failure("Error registering account", ex);
        }
    }

    public ServiceResult<AuthResponse> login(LoginRequest request) {
        try {
            var accountOptional = accountRepository.findByEmail(request.email());
            if (accountOptional.isEmpty())
                return ServiceResult.failure("Invalid credentials");

            var account = accountOptional.get();

            if (!passwordEncoder.matches(request.password(), account.getPasswordHash()))
                return ServiceResult.failure("Invalid credentials");

            String token = jwtService.generateToken(account);
            var data = new AuthResponse(token, jwtExpiration, account.getUsername(), account.getEmail());
            return ServiceResult.success("Login successfully", data);
        }
        catch (Exception ex){
            return ServiceResult.failure("Error when logged in", ex);
        }
    }

    public ServiceResult<Void> confirmEmail(UUID userId, String token) {
        try {
            if (userId == null) {
                return ServiceResult.failure("User id is invalid");
            }

            if (token == null || token.trim().isEmpty()) {
                return ServiceResult.failure("Token is required");
            }

            var accountOptional = accountRepository.findById(userId);
            if (accountOptional.isEmpty()) {
                return ServiceResult.failure("User not found");
            }

            var tokenUserId = jwtService.extractSubject(token);
            if (!userId.toString().equals(tokenUserId)) {
                return ServiceResult.failure("Invalid token");
            }

            var account = accountOptional.get();
            if (Boolean.TRUE.equals(account.getEmailVerified())) {
                return ServiceResult.success("Email already confirmed");
            }

            account.setEmailVerified(true);
            accountRepository.save(account);

            return ServiceResult.success("Email confirmed successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Invalid token", ex);
        }
    }
}
