package com.beca.ms1_users.adapters.out;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, UUID>  {
    boolean existsByEmailIgnoreCase(String email);
}