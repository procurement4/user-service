package com.alterra.user.service.auth.controller;

import com.alterra.user.service.users.repository.UserRepositoryJPA;
import com.alterra.user.service.utils.ResponseAPI;
import com.alterra.user.service.config.JwtUtils;
import com.alterra.user.service.auth.model.AuthRequest;
import com.alterra.user.service.auth.model.AuthResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserRepositoryJPA userRepositoryJPA;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ResponseAPI responseAPI;
    private final Gson gson;
    @Value("${BASE_URL}")
    private String BASE_URL;
    @Value("[auth-service]")
    private String SERVICE_NAME;
    @GetMapping
    public ResponseEntity hello(){
        return new ResponseEntity("User-Service is Online",HttpStatus.OK);
    }

    @PostMapping("/v1/auth/login")
    public ResponseEntity authenticate(@RequestBody AuthRequest request){
        log.info(String.format("%s POST : %s /api/v1/auth/login is called", SERVICE_NAME, BASE_URL));
        log.info(String.format("%s Request : %s", SERVICE_NAME, gson.toJson(request)));
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            var getUserByEmail = userRepositoryJPA.findByEmail(request.getEmail());
            if (getUserByEmail == null) return new ResponseEntity(responseAPI.BAD_REQUEST("Email not found", null), HttpStatus.BAD_REQUEST);
            if (getUserByEmail.getIs_active().equals(false)) return new ResponseEntity(responseAPI.BAD_REQUEST("Please verification your account", null), HttpStatus.BAD_REQUEST);
            var data = new AuthResponse();
            data.setToken(jwtUtils.generateToken(request.getEmail()));
            data.setUser_id(getUserByEmail.getId().toString());
            return new ResponseEntity(responseAPI.OK("Success authentication", data), HttpStatus.OK);
        }catch (Exception ex){
            log.error(String.format("%s Exception : Invalid email and password", SERVICE_NAME));
            return new ResponseEntity(responseAPI.BAD_REQUEST("Invalid email and password", null), HttpStatus.BAD_REQUEST);
        }
    }
}
