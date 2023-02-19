package com.alterra.user.service.users.service;

import com.alterra.user.service.users.model.ActivateUserRequest;
import com.alterra.user.service.users.model.ResetPasswordRequest;
import com.alterra.user.service.utils.Email;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaListenersService {
    private final Email email;
    @Value("[kafka-subscribe-service]")
    private String SERVICE_NAME;

    @KafkaListener(topics = "resetPassword")
    public void subscribeResetPassword(String request){
        log.info(String.format("%s subscribeResetPassword is called", SERVICE_NAME));
        var objRequest = new Gson().fromJson(request, ResetPasswordRequest.class);
        var sendEmail = email.sendEmailResetPassword(objRequest);
        if (sendEmail){
            log.info(String.format("%s Success send reset password email to : " + objRequest.getEmail(), SERVICE_NAME));
        }
        else {
            log.error(String.format("%s Error send email reset password, please check your email", SERVICE_NAME));
        }
    }

    @KafkaListener(topics = "activateUser")
    public void subscribeActivateUser(String request){
        log.info(String.format("%s subscribeActivateUser is called", SERVICE_NAME));
        var objRequest = new Gson().fromJson(request, ActivateUserRequest.class);
        var sendEmail = email.sendEmailActivateUser(objRequest);
        if (sendEmail){
            log.info(String.format("%s Success send activate email to : " + objRequest.getEmail(), SERVICE_NAME));
        }
        else {
            log.error(String.format("%s Error send email activate, please check your email", SERVICE_NAME));
        }
    }
}
