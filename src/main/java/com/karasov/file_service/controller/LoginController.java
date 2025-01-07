package com.karasov.file_service.controller;

import com.karasov.file_service.dto.ErrorResponseDto;
import com.karasov.file_service.dto.LoginRequestDto;
import com.karasov.file_service.dto.LoginResponseDto;
import com.karasov.file_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки запросов на вход в систему (логин).
 */
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    /**
     * Метод для аутентификации пользователя и выдачи токена.
     *
     * @param loginRequestDto объект, содержащий логин и пароль пользователя
     * @return ResponseEntity с токеном в случае успешной аутентификации
     * или с ошибкой в случае неудачи
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginMethod(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            String token = authService.authenticateAndIssueToken(loginRequestDto.login(), loginRequestDto.password());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new ErrorResponseDto("Bad credentials", 400),
                    HttpStatus.valueOf(400)
            );
        }
    }

    /**
     * Метод для выхода из системы.
     *
     * @param token токен аутентифицированного пользователя для выхода
     * @return ResponseEntity с сообщением об успешном выходе
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutMethod(@RequestHeader("auth-token") String token) {
        return ResponseEntity.ok("Success logout");
    }
}

