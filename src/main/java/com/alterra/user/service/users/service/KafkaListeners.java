package com.alterra.user.service.users.service;

import com.alterra.user.service.users.model.ResetPasswordRequest;
import com.alterra.user.service.utils.Email;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaListeners {
    private final Email email;
    @KafkaListener(topics = "resetPassword")
    public void subscribeResetPassword(String request){
        var objRequest = new Gson().fromJson(request, ResetPasswordRequest.class);
        var sendEmail = email.sendEmailResetPassword(objRequest);
        if (sendEmail){
            log.info("Success send email to : " + objRequest.getEmail());
        }
        else {
            log.error("Error send email, please check your email");
        }
    }
}
