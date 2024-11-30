package com.karasov.file_service.service;

import com.karasov.file_service.model.SystemUserDetails;
import com.karasov.file_service.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemUserDetailService implements UserDetailsService {

    private final SystemUserRepository userRepository;

    /**
     * Метод для загрузки пользователя по адресу электронной почты.
     *
     * <p>Ищет пользователя в базе данных по переданному email. Если пользователь найден,
     * возвращает объект {@link UserDetails}, представляющий пользователя, иначе выбрасывает
     * исключение {@link UsernameNotFoundException}.</p>
     *
     * @param email Адрес электронной почты пользователя.
     * @return {@link UserDetails} объект, представляющий пользователя.
     * @throws UsernameNotFoundException если пользователь с указанным email не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(SystemUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with e-mail %s not found.", email)
                ));
    }
}
