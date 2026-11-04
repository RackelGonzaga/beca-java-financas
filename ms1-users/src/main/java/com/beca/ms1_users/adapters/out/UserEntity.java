
package com.beca.ms1_users.adapters.out;

import java.util.UUID;

import com.beca.ms1_users.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    protected UserEntity() {
    }

    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();

        entity.id = user.getId();
        entity.nome = user.getNome();
        entity.email = user.getEmail();
        entity.senha = user.getSenha();

        return entity;
    }

    public User toDomain() {
        return new User(id, nome, email, senha);
    }

    public UUID getId() {
        return id;
    }
}