package com.alterra.user.service.users.repository;

import com.alterra.user.service.users.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.UUID;

public interface UserRepositoryJPA extends JpaRepository<User, UUID> {
    User findByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "update users set password = :password, updated_at = :updated_at where id = :user_id", nativeQuery = true)
    void resetPassword(@Param("password") String password, @Param("updated_at") Date updated_at, @Param("user_id") UUID user_id);
}
