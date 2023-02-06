package com.alterra.user.service.users.service;

import com.alterra.user.service.common.ResponseAPI;
import com.alterra.user.service.users.entity.User;
import com.alterra.user.service.users.model.UserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    User findByEmail(String email);
//    List<User> getAllUsers();
    ResponseAPI getAllUsers();
    ResponseAPI getUserById(UUID userId);
    ResponseAPI createUser(UserRequest request);
    ResponseAPI updateUser(UserRequest request);
    ResponseAPI uploadProfile(MultipartFile file);
}
