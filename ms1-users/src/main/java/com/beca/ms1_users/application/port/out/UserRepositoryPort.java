package com.beca.ms1_users.application.port.out;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import com.beca.ms1_users.domain.User;


public interface UserRepositoryPort {

    boolean existsByEmail(String email);

    User save(User user);

    List<User> findAll();
    
    Optional<User> findById(UUID id);

    void delete(User user);

}