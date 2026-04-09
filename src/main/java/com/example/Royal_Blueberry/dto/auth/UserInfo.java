package com.example.Royal_Blueberry.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class UserInfo implements Serializable {
    private String id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String role;
}
