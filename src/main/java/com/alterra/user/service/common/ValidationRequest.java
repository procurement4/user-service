package com.alterra.user.service.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ValidationRequest<T> {
    private T data;
    ValidatorFactory validatorFactory;
    Validator validator;

    public ValidationRequest(T data) {
        this.data = data;
    }

    public List<String> validate(){
        List<String> errMsg = new ArrayList<>();
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(data);

        if (violations.size() > 0) {
            for (ConstraintViolation<T> violation : violations) {
                errMsg.add(violation.getMessage());
            }
        }
        return errMsg;
    }
}
