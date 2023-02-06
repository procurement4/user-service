package com.alterra.user.service.users.service;

import com.alterra.user.service.common.Json;
import com.alterra.user.service.common.ResponseAPI;
import com.alterra.user.service.common.ValidationRequest;
import com.alterra.user.service.users.entity.User;
import com.alterra.user.service.users.model.UploadImageResponse;
import com.alterra.user.service.users.model.UserRequest;
import com.alterra.user.service.users.model.UserResponse;
import com.alterra.user.service.users.repository.UserRepositoryJPA;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final UserRepositoryJPA userRepositoryJPA;
    private final ResponseAPI responseAPI;
    @Autowired
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Value("${BASE_URL}")
    private String BASE_URL;
    @Value("[auth-service]")
    private String SERVICE_NAME;
    public User findByEmail(String email) {
        return userRepositoryJPA.findByEmail(email);
    }

    public ResponseAPI getAllUsers(){
        try {
            log.info(String.format("%s userService.getAllUsers is called", SERVICE_NAME));
            var getAllUsers = userRepositoryJPA.findAll();
            var data = getAllUsers.stream().map(x -> modelMapper.map(x, UserResponse.class)).toList();
            if (getAllUsers.size() > 0){
                log.info(String.format("%s Result : %s", SERVICE_NAME, new Gson().toJson(data)));
                return responseAPI.OK("Success get data", data);
            }
            log.info(String.format("%s Result : %s", SERVICE_NAME, new Gson().toJson(data)));
            return responseAPI.OK("Success get data", getAllUsers);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI getUserById(UUID userId){
        try {
            log.info(String.format("%s userService.getUserById is called", SERVICE_NAME));
            var getUserById = userRepositoryJPA.findById(userId);
            var data = modelMapper.map(getUserById, UserResponse.class);
            log.info(String.format("%s Result : %s", SERVICE_NAME, new Gson().toJson(data)));
            if (getUserById.isEmpty()) return responseAPI.NOT_FOUND("User not found", null);
            return responseAPI.OK("Success get data", data);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI createUser(UserRequest request){
        try {
            log.info(String.format("%s userService.createUser is called", SERVICE_NAME));
            log.info(String.format("%s Request : %s", SERVICE_NAME, new Gson().toJson(request)));
            //Validate request
            var validate = new ValidationRequest(request).validate();
            if (validate.size() > 0){
                log.info(String.format("Validate Error : %s", validate.toString()));
                return responseAPI.BAD_REQUEST(validate.toString(), null);
            }

            //Check email exist
            var getByEmail = userRepositoryJPA.findByEmail(request.getEmail());
            if (getByEmail != null) return responseAPI.BAD_REQUEST("Email is already registered", null);

            var newUser = modelMapper.map(request, User.class);
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setPhoto_profile("");
            newUser.setIs_active(false);
            newUser.setCreated_at(new Date());
            newUser.setUpdated_at(new Date());
            userRepositoryJPA.save(newUser);

            //Check data saved
            var getUserById = userRepositoryJPA.findById(newUser.getId());
            if (getUserById.isEmpty()) {
                log.info(String.format("%s User not found", SERVICE_NAME));
                return responseAPI.INTERNAL_SERVER_ERROR("Failed create new user", null);
            }

            log.info(String.format("%s Data successfully created", SERVICE_NAME));
            var data = modelMapper.map(getUserById, UserResponse.class);
            return responseAPI.CREATED("Success create new user", data);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI updateUser(UserRequest request){
        try {
            //Validate request
            var validate = new ValidationRequest(request).validate();
            if (validate.size() > 0) return responseAPI.BAD_REQUEST(validate.toString(), null);

            //Check data exist
            var getUserById = userRepositoryJPA.findById(request.getId());
            if (getUserById.isEmpty()) return responseAPI.INTERNAL_SERVER_ERROR("User not found", null);
            if (getUserById.isEmpty()) return responseAPI.INTERNAL_SERVER_ERROR("User not found", null);

            var updatedUser = modelMapper.map(request, User.class);
            updatedUser.setPassword(passwordEncoder.encode(request.getPassword()));
            updatedUser.setCreated_at(getUserById.get().getCreated_at());
            updatedUser.setUpdated_at(new Date());
            userRepositoryJPA.save(updatedUser);
            var data = modelMapper.map(updatedUser, UserResponse.class);
            return responseAPI.OK("Success update user data", data);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI uploadProfile(MultipartFile file){
        try {
            String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/images";

            StringBuilder fileNames = new StringBuilder();
            Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
            if (Files.notExists(Paths.get(UPLOAD_DIRECTORY))){
                File newDirectory = new File(System.getProperty("user.dir"), "images");
                newDirectory.mkdir();
            }
            fileNames.append(fileNameAndPath);
            Files.write(fileNameAndPath, file.getBytes());
            var data = new UploadImageResponse(fileNameAndPath.toString());
            return responseAPI.OK("Success upload image", data);
        }catch (IOException ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }
}
