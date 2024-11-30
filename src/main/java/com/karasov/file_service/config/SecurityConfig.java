package com.karasov.file_service.config;

import com.karasov.file_service.service.SystemUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SystemUserDetailService userDetailsService;

    /**
     * Бин для {@link UserDetailsService}, который используется для загрузки пользователя.
     *
     * <p>Этот метод возвращает {@link UserDetailsService}, которое загружает данные о пользователе
     * из базы данных через {@link SystemUserDetailService}.</p>
     *
     * @return объект {@link UserDetailsService} для аутентификации пользователей.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return this.userDetailsService;
    }

    /**
     * Бин для {@link AuthenticationManager}, который используется для аутентификации пользователей.
     *
     * <p>Этот метод настраивает и возвращает объект {@link AuthenticationManager} из {@link AuthenticationConfiguration}.</p>
     *
     * @param configuration конфигурация для аутентификации.
     * @return объект {@link AuthenticationManager} для обработки аутентификации.
     * @throws Exception если возникает ошибка при получении {@link AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Бин для {@link AuthenticationProvider}, который используется для аутентификации через {@link DaoAuthenticationProvider}.
     *
     * <p>Этот метод настраивает {@link DaoAuthenticationProvider} с {@link UserDetailsService} и {@link PasswordEncoder} для
     * выполнения аутентификации.</p>
     *
     * @return настроенный {@link AuthenticationProvider}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Бин для {@link SecurityFilterChain}, который настраивает фильтры безопасности для HTTP-запросов.
     *
     * <p>Этот метод настраивает фильтры безопасности, разрешая доступ к /login без аутентификации, а остальные запросы
     * требуют аутентификации. Также включается пользовательский {@link AuthenticationProvider} для аутентификации.</p>
     *
     * @param http объект для конфигурации фильтров безопасности.
     * @return настроенный {@link SecurityFilterChain}.
     * @throws Exception если возникает ошибка при настройке безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider());
        return http.build();
    }

    /**
     * Бин для {@link PasswordEncoder}, который используется для кодирования паролей пользователей.
     *
     * <p>Этот метод возвращает {@link BCryptPasswordEncoder}, который является безопасным методом для кодирования паролей.</p>
     *
     * @return объект {@link PasswordEncoder} для кодирования паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

