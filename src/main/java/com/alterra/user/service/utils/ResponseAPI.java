package com.alterra.user.service.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseAPI<T> {
    private int code;
    private String message;
    private String status;
    private T data;

    public ResponseAPI OK(String message, T data){
        return new ResponseAPI<>(
                HttpStatus.OK.value(),
                message,
                HttpStatus.OK.getReasonPhrase(),
                data
        );
    }

    public ResponseAPI CREATED(String message, T data){
        return new ResponseAPI<>(
                HttpStatus.CREATED.value(),
                message,
                HttpStatus.CREATED.getReasonPhrase(),
                data
        );
    }

    public ResponseAPI INTERNAL_SERVER_ERROR(String message, T data){
        return new ResponseAPI<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                data
        );
    }

    public ResponseAPI BAD_REQUEST(String message, T data){
        return new ResponseAPI<>(
                HttpStatus.BAD_REQUEST.value(),
                message,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                data
        );
    }

    public ResponseAPI NOT_FOUND(String message, T data){
        return new ResponseAPI<>(
                HttpStatus.NOT_FOUND.value(),
                message,
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                data
        );
    }
}
