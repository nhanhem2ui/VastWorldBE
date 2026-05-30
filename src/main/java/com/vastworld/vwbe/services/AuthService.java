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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Long jwtExpiration;
    private final EmailService emailService;
    private final String baseUrl;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, @Value("${jwt.expiration}") Long jwtExpiration,
                       EmailService emailService, @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtExpiration = jwtExpiration;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
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

            String token = UUID.randomUUID().toString();
            account.setEmailVerificationToken(token);
            account.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
            accountRepository.save(account);

            String confirmationLink = "%s/api/auth/confirm-email?userId=%s&token=%s".formatted(
                    baseUrl,
                    account.getId(),
                    URLEncoder.encode(token, StandardCharsets.UTF_8)
            );
            String emailBody = """
        <p style="font-size:16px;">
            Thank you for registering! Please confirm your email address to activate your account.
        </p>

        <div style="margin:30px 0;text-align:center;">
            <a href="%s"
               style="
                    background:#0078D4;
                    color:white;
                    padding:14px 28px;
                    text-decoration:none;
                    border-radius:6px;
                    font-weight:bold;
                    display:inline-block;
               ">
                Confirm Email
            </a>
        </div>

        <p style="
            background:#FFF3CD;
            color:#856404;
            padding:12px;
            border-radius:6px;
            border:1px solid #FFE69C;
        ">
            This verification link expires in 24 hours.
        </p>

        <p style="font-size:14px;color:#666;">
            If you did not create this account, you can safely ignore this email.
        </p>
        """.formatted(confirmationLink);

            emailService.sendEmail(account.getEmail(), "Confirm your email", emailBody);

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

            if (!account.getEmailVerified()) {
                return ServiceResult.failure("Email not verified");
            }

            if (Boolean.TRUE.equals(account.getIsBanned())) {
                return ServiceResult.failure("Account is banned");
            }

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

            var account = accountOptional.get();
            if (Boolean.TRUE.equals(account.getEmailVerified())) {
                return ServiceResult.success("Email already confirmed");
            }

            if (!token.equals(account.getEmailVerificationToken())) {
                return ServiceResult.failure("Invalid token");
            }

            if (account.getEmailVerificationExpiry() == null
                    || account.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
                return ServiceResult.failure("Token expired");
            }

            account.setEmailVerified(true);
            account.setEmailVerificationToken(null);
            account.setEmailVerificationExpiry(null);
            accountRepository.save(account);

            return ServiceResult.success("Email confirmed successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Invalid token", ex);
        }
    }
}
