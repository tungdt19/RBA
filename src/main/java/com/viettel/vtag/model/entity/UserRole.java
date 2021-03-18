package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;

@Data
@Accessors(fluent = true)
public class UserRole implements GrantedAuthority {

    private int id;
    private int userId;
    private int roleId;
    private int deviceId;

    private String role;

    @Override
    public String getAuthority() {
        return role;
    }
}
