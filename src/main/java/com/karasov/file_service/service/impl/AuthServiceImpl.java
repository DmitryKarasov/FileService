package com.karasov.file_service.service.impl;

import com.karasov.file_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Сервис для аутентификации пользователей и генерации JWT-токенов.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Аутентифицирует пользователя по логину и паролю и генерирует JWT-токен.
     *
     * @param login    Логин пользователя.
     * @param password Пароль пользователя.
     * @return Сгенерированный JWT-токен для доступа к системе.
     * @throws AuthenticationException Если учетные данные пользователя неверны или аутентификация не удалась.
     */
    @Override
    public String authenticateAndIssueToken(String login, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        return jwtService.generateToken(authentication.getName());
    }
}


