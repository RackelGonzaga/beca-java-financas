package com.beca.ms1_users.infrastructure.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.User;

@Service
public class Ms1UserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepository;

    public Ms1UserDetailsService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        String email = username.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado"
                ));

        return new UserPrincipal(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getSenha()
        );
    }
}