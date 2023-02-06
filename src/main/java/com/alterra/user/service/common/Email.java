package com.alterra.user.service.common;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class Email {
    public void validate(String emailAddress) {
        emailAddress = "username@domain.com";
        var regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        var test =  regexPattern.matches(emailAddress);
        //assertTrue(EmailValidation.patternMatches(emailAddress, regexPattern));
    }
}
