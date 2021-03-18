package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class User {

    private int id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String avatar;

    private String encryptedPassword;
}
