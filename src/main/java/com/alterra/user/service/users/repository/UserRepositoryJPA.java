package com.alterra.user.service.users.repository;

import com.alterra.user.service.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepositoryJPA extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
