package com.beca.ms1_users.application.port.in;

import java.util.List;
import java.util.UUID;

import com.beca.ms1_users.domain.User;

public interface UserUseCase {

    User registerUser(RegisterUserCommand command);

    List<User> findAllUsers();

    User findUserById(UUID id);

    User updateUser(UUID id, UpdateUserCommand command);

    void deleteUser(UUID id);

    ImportUsersResult importUsers(ImportUsersCommand command);

    User authenticateUser(AuthenticateUserCommand command);
}