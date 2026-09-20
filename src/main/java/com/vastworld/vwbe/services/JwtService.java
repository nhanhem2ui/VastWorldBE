package com.vastworld.vwbe.services;

import com.vastworld.vwbe.entites.Account;
import com.vastworld.vwbe.repositories.PlayerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private final PlayerRepository playerRepository;

    public JwtService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public String generateToken(Account account) {

        var player = playerRepository.findPlayerByAccount_Id(account.getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        var now = new Date();
        var expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(account.getId().toString())
                .claim("playerId", player.getId().toString())
                .claim("email", account.getEmail())
                .claim("username", account.getUsername())
                .claim("role", account.getRole())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                ))
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
    public UUID extractPlayerId(String token) {
        String playerId = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                ))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("playerId", String.class);

        return UUID.fromString(playerId);
    }
}
