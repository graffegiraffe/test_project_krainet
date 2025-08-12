package by.rublevskaya.authservice.security;

import by.rublevskaya.authservice.model.Security;
import by.rublevskaya.authservice.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final SecurityRepository securityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Security security = securityRepository.findByLogin(username);
        if (security == null) {
            throw new UsernameNotFoundException("User not found with login: " + username);
        }
        log.info("Found user: " + security.getLogin());
        return User.builder()
                .username(security.getLogin())
                .password(security.getPassword())
                .roles(security.getRole())
                .build();
    }
}