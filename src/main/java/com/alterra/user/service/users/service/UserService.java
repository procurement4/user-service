package com.alterra.user.service.users.service;

import com.alterra.user.service.users.model.ResetPasswordRequest;
import com.alterra.user.service.utils.ResponseAPI;
import com.alterra.user.service.users.entity.User;
import com.alterra.user.service.users.model.UserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {
    User findByEmail(String email);
    ResponseAPI getAllUsers();
    ResponseAPI getUserById(String userId);
    ResponseAPI createUser(UserRequest request);
    ResponseAPI updateUser(UserRequest request);
    ResponseAPI uploadProfile(MultipartFile file);
    ResponseAPI saveImages(MultipartFile file);
    ResponseAPI resetPassword(ResetPasswordRequest request);
    ResponseAPI activateUser(String userId);
}
