package com.alterra.user.service.auth.controller;

import com.alterra.user.service.common.ResponseAPI;
import com.alterra.user.service.auth.config.JwtUtils;
import com.alterra.user.service.auth.model.AuthRequest;
import com.alterra.user.service.auth.model.AuthResponse;
import com.alterra.user.service.auth.service.AuthServiceImpl;
import com.alterra.user.service.exception.RestResponseEntityExceptionHandler;
import com.google.gson.Gson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
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
        return new ResponseEntity("User-Service",HttpStatus.OK);
    }

    @PostMapping("/v1/auth/login")
    public ResponseEntity authenticate(@RequestBody AuthRequest request){
        log.info(String.format("%s POST : %s /api/v1/auth/login is called", SERVICE_NAME, BASE_URL));
        log.info(String.format("%s Request : %s", SERVICE_NAME, gson.toJson(request)));
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            var token = new AuthResponse(jwtUtils.generateToken(request.getEmail()));
            return new ResponseEntity(responseAPI.OK("Success authentication", token), HttpStatus.OK);
        }catch (Exception ex){
            log.error(String.format("%s Exception : Invalid email and password"), SERVICE_NAME);
            return new ResponseEntity(responseAPI.BAD_REQUEST("Invalid email and password", null), HttpStatus.BAD_REQUEST);
        }
    }
}
