package com.beca.ms1_users.adapters.out;

import com.beca.ms1_users.application.port.out.UserRepositoryPort;
import com.beca.ms1_users.domain.User;
import com.beca.ms1_users.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component 
public class UserRepositoryAdapter implements UserRepositoryPort {
    
    private final UserRepository userRepository;

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
    return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

}