package com.alterra.user.service.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    UserDetails findByEmail(String email);
}
