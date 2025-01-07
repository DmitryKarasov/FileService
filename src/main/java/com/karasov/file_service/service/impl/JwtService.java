package com.karasov.file_service.service.impl;

import com.karasov.file_service.handler.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Сервис для работы с JWT-токенами.
 */
@Slf4j
@Component
public class JwtService {

    @Value("${jwt.expiration}")
    private long expirationTime;  // Время действия токена в секундах.
    @Value("${jwt.secret}")
    private String secretKeyString;   // Секретный ключ для подписи токенов.
    @Getter
    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует JWT-токен для указанного имени пользователя.
     *
     * @param username Имя пользователя, для которого генерируется токен.
     * @return Подписанный JWT-токен в виде строки.
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationTime);

        String randomString = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(username)
                .claim("random", randomString)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Валидирует JWT-токен и извлекает из него имя пользователя.
     *
     * @param token JWT-токен для проверки и извлечения имени пользователя.
     * @return Имя пользователя, если токен валиден.
     * @throws InvalidTokenException Если токен невалиден.
     */
    public String validateAndExtractUsername(String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = extractClaims(token);
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Невалидный токен: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }

    /**
     * Парсит и проверяет подпись переданного JWT-токена
     *
     * @param token JWT-токен, из которого извлекаются клеймы.
     * @throws JwtException Если токен невалиден, либо подпись не совпадает с ожиданиями.
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

