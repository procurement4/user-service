package com.alterra.user.service.users.controller;

import com.alterra.user.service.users.model.UserRequest;
import com.alterra.user.service.users.service.UserService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
//@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
public class UserController {
    private static final String API_KEY = "3f7afc6d1e818f8646ea5363a29c9541-ca9eeb88-8fceec84";
    private static final String YOUR_DOMAIN_NAME = "procurement-capstone.site";
    private final UserService userService;
    @Value("${BASE_URL}")
    private String BASE_URL;
    @Value("[auth-service]")
    private String SERVICE_NAME;

    public UserController(UserService userService, KafkaTemplate<String, String> kafkaTemplate) {
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
    }

    private KafkaTemplate<String,String> kafkaTemplate;

    @GetMapping("/v1/users")
    public ResponseEntity getAllUsers(){
        log.info(String.format("%s GET : %s /api/v1/users is called", SERVICE_NAME, BASE_URL));
        var result = userService.getAllUsers();
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @GetMapping("/v1/users/{userId}")
    public ResponseEntity getUserById(@PathVariable String userId){
        log.info(String.format("%s GET : %s /api/v1/users/{userId} is called", SERVICE_NAME, BASE_URL));
        var result = userService.getUserById(userId);
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @PostMapping("/v1/register")
    public ResponseEntity createUser(@RequestBody UserRequest request){
        var result = userService.createUser(request);
        if (result.getCode() == 200){
            kafkaTemplate.send("activateUser", new Gson().toJson(result.getData()));
        }
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
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @GetMapping("/v1/reset_password/{userId}")
    public ResponseEntity resetPassword(@PathVariable String userId) {
       var result = userService.resetPassword(userId);
       if (result.getCode() == 200){
           kafkaTemplate.send("resetPassword", new Gson().toJson(result.getData()));
       }
       result.setData(null);
       return ResponseEntity.status(result.getCode()).body(result);
    }

    @GetMapping("/v1/activate/{userId}")
    public ResponseEntity activateUser(@PathVariable String userId){
        var result = userService.activateUser(userId);
        if (result.getCode() == 200){
            return ResponseEntity.status(200).body("Your account is active now!");
        }
        return ResponseEntity.status(result.getCode()).body(result);
    }
}
