package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class User {

    private int id;

    @JsonProperty("phone")
    private String phoneNo;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;

    private String encryptedPassword;
}
