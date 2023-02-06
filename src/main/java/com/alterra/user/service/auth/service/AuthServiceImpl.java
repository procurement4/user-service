package com.alterra.user.service.auth.service;

import com.alterra.user.service.users.entity.User;
import com.alterra.user.service.users.repository.UserRepositoryJPA;
import com.alterra.user.service.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepositoryJPA userRepositoryJPA;
    private final UserService userService;
    public UserDetails findByEmail(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
