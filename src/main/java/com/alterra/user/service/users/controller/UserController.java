package com.alterra.user.service.users.controller;

import com.alterra.user.service.users.model.UserRequest;
import com.alterra.user.service.users.service.UserService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final Gson gson;
    @Value("${BASE_URL}")
    private String BASE_URL;
    @Value("[auth-service]")
    private String SERVICE_NAME;

    @GetMapping("/v1/users")
    public ResponseEntity getAllUsers(){
        log.info(String.format("%s GET : %s /api/v1/users is called", SERVICE_NAME, BASE_URL));
        var result = userService.getAllUsers();
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @GetMapping("/v1/users/{userId}")
    public ResponseEntity getUserById(@PathVariable UUID userId){
        log.info(String.format("%s GET : %s /api/v1/users/{userId} is called", SERVICE_NAME, BASE_URL));
        var result = userService.getUserById(userId);
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @PostMapping("/v1/register")
    public ResponseEntity createUser(@RequestBody UserRequest request){
        var result = userService.createUser(request);
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @PatchMapping("/v1/users")
    public ResponseEntity updateUser(@RequestBody UserRequest request){
        var result = userService.updateUser(request);
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @PostMapping("/v1/upload")
    public ResponseEntity uploadImage(@RequestParam("image") MultipartFile file){
        var result = userService.uploadProfile(file);
        return ResponseEntity.status(result.getCode()).body(result);
    }
    @PostMapping("/v1/upload/gcp")
    public ResponseEntity uploadGCP(@RequestParam("image") MultipartFile file) {
        var result = userService.saveImages(file);
        return ResponseEntity.status(200).body(result);
    }
}
