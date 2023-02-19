package com.alterra.user.service.users.service;

import com.alterra.user.service.users.model.ResetPasswordRequest;
import com.alterra.user.service.utils.Email;
import com.alterra.user.service.utils.FileUtils;
import com.alterra.user.service.utils.ResponseAPI;
import com.alterra.user.service.utils.ValidationRequest;
import com.alterra.user.service.users.entity.User;
import com.alterra.user.service.users.model.UploadImageResponse;
import com.alterra.user.service.users.model.UserRequest;
import com.alterra.user.service.users.model.UserResponse;
import com.alterra.user.service.users.repository.UserRepositoryJPA;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    @Value("${GCP_BUCKET_NAME}")
    private String BUCKET_NAME;
    @Value("${GCP_BUCKET_URL}")
    private String BUCKET_URL;
    @Value("${GCP_PROJECT_ID}")
    private String PROJECT_ID;
    @Value("${GCP_CREDENTIALS}")
    private String GOOGLE_CREDENTIALS;
    private final FileUtils fileUtils;

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

    public ResponseAPI getUserById(String userId){
        try {
            log.info(String.format("%s userService.getUserById is called", SERVICE_NAME));
            var id = UUID.fromString(userId);
            var getUserById = userRepositoryJPA.findById(id);
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
            data.setId(newUser.getId());
            return responseAPI.OK("Success create new user", data);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI updateUser(UserRequest request){
        try {
            //Validate request
            var id = UUID.fromString(request.getId());
            var validate = new ValidationRequest(request).validate();
            if (validate.size() > 0) return responseAPI.BAD_REQUEST(validate.toString(), null);

            //Check data exist
            var getUserById = userRepositoryJPA.findById(id);
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
                java.io.File newDirectory = new java.io.File(System.getProperty("user.dir"), "images");
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

    public ResponseAPI saveImages(MultipartFile file){
        try {
            Credentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(GOOGLE_CREDENTIALS));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials)
                    .setProjectId(PROJECT_ID).build().getService();

            String fileName = System.nanoTime() + "_" + file.getOriginalFilename();
            var isValidSize = fileUtils.checkImageSize(file);
            var isValidExtension = fileUtils.checkFileExtension(file.getOriginalFilename());
            if (!isValidSize) return responseAPI.BAD_REQUEST("Max size of image is 5MB",null);
            if (!isValidExtension) return responseAPI.BAD_REQUEST("Extension must be .png, .jpeg, .jpg",null);

            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder(BUCKET_NAME, fileName)
                            .setContentType(file.getContentType())
                            .setAcl(new ArrayList<>(
                                    Arrays.asList(Acl.of(Acl.User.ofAllUsers(),Acl.Role.READER))))
                            .build(),
                    file.getInputStream());
            String imageUrl = BUCKET_URL + BUCKET_NAME + "/" + fileName;
            var data = new UploadImageResponse(imageUrl);
            return responseAPI.OK("Success upload image", data);
        }catch (IOException ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI resetPassword(String userId){
        try {
            var id = UUID.fromString(userId);
            var getUserById = userRepositoryJPA.findById(id);
            if (getUserById.isEmpty()) return responseAPI.INTERNAL_SERVER_ERROR("User not found", null);
            String newPassword = RandomStringUtils.randomAlphabetic(6);
            var resetPasswordRequest = new ResetPasswordRequest();
            resetPasswordRequest.setUser_id(userId);
            resetPasswordRequest.setNew_password(newPassword);
            resetPasswordRequest.setEmail("hendralw98@gmail.com");
            userRepositoryJPA.resetPassword(passwordEncoder.encode(newPassword), new Date(), id);
            return responseAPI.OK("Success reset password, please check your email at " + resetPasswordRequest.getEmail() , resetPasswordRequest);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }

    public ResponseAPI activateUser(String userId){
        try {
            var id = UUID.fromString(userId);
            var getUserById = userRepositoryJPA.findById(id);
            if (getUserById.isEmpty()) return responseAPI.INTERNAL_SERVER_ERROR("User not found", null);
            userRepositoryJPA.activateUser(id, new Date());
            return responseAPI.OK("Success activate user", null);
        }catch (Exception ex){
            var errMsg = String.format("Error Message : %s with Stacktrace : %s",ex.getMessage(),ex.getStackTrace());
            log.error(String.format("%s" , errMsg));
            return responseAPI.INTERNAL_SERVER_ERROR(errMsg,null);
        }
    }
}
