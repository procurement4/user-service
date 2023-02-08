package com.alterra.user.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class Json<T> {
    public String toJson(T obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String strJson = mapper.writeValueAsString(obj);
        return strJson;
    }
}
