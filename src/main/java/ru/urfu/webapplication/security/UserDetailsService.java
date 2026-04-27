package ru.urfu.webapplication.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.repository.UserRepository;

@Service
@RequiredArgsConstructor
class WeatherUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Загрузка пользователя
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        return new WeatherUserDetails(user);
    }

    public UserDetails loadUserByApiKey(String apiKey) throws UsernameNotFoundException {
        User user = userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new UsernameNotFoundException("Неверный API ключ"));
        return new WeatherUserDetails(user);
    }
}