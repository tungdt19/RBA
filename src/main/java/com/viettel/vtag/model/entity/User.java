package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class User {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("phone")
    private String phoneNo;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    private String avatar;
    private UUID platformId;
    private String encryptedPassword;
    private String fcmToken;
}
