package com.karasov.file_service.filter;

import com.karasov.file_service.service.impl.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для обработки аутентификации с использованием JWT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Метод, выполняющий фильтрацию запроса на основе JWT.
     * <p>
     * 1. Извлекает токен из заголовка запроса "auth-token".
     * 2. Пропускает запросы на пути "/login" и "/logout" без проверки токена.
     * 3. Если токен отсутствует или невалиден, устанавливает статус 401 (Unauthorized) и завершает обработку.
     * 4. Если токен валиден, извлекает имя пользователя, загружает его детали с помощью {@link UserDetailsService},
     * и устанавливает аутентификацию в {@link SecurityContextHolder}.
     * </p>
     *
     * @param request     объект запроса, содержащий информацию о текущем запросе.
     * @param response    объект ответа, в который может быть записан статус.
     * @param filterChain цепочка фильтров, которая продолжает обработку запроса.
     * @throws ServletException в случае ошибки при обработке запроса.
     * @throws IOException      в случае ошибки ввода-вывода.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authToken = request.getHeader("auth-token");
        String path = request.getServletPath();

        if (path.equals("/login") || path.equals("/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = jwtService.validateAndExtractUsername(authToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Аутентификация установлена для пользователя: {}", username);
            }
        }
        filterChain.doFilter(request, response);
    }
}



