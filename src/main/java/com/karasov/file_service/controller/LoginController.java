package com.karasov.file_service.controller;

import com.karasov.file_service.dto.ErrorResponseDto;
import com.karasov.file_service.dto.LoginRequestDto;
import com.karasov.file_service.dto.LoginResponseDto;
import com.karasov.file_service.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Метод для аутентификации пользователя и генерации JWT токена.
     *
     * <p>Принимает запрос с логином и паролем пользователя, выполняет аутентификацию и,
     * если аутентификация прошла успешно, генерирует и возвращает JWT токен.</p>
     *
     * @param loginRequestDto объект, содержащий данные для входа (логин и пароль).
     * @return {@link ResponseEntity} с JWT токеном, если аутентификация прошла успешно,
     * или с ошибкой, если аутентификация не удалась.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginMethod(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            // Создаем токен для аутентификации пользователя
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequestDto.login(),
                            loginRequestDto.password());

            // Аутентифицируем пользователя
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // Генерируем JWT токен
            String token = jwtTokenUtil.generateToken(authentication.getName());

            // Возвращаем токен в ответе
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (AuthenticationException e) {

            // В случае ошибки аутентификации возвращаем ошибку
            return new ResponseEntity<>(new ErrorResponseDto("Invalid login credentials", 400),
                    HttpStatus.valueOf(400));
        }
    }
}
