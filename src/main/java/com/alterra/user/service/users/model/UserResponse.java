package com.alterra.user.service.users.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String rolename;
    private String photo_profile;
    private Boolean is_active;
    private Date created_at;
    private Date updated_at;
}
