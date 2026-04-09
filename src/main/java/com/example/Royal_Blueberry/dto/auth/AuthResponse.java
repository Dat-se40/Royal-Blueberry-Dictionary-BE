package com.example.Royal_Blueberry.dto.auth;

import lombok.Builder;
import lombok.Getter;


import java.io.Serializable;

@Getter
@Builder
public class AuthResponse implements Serializable {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private UserInfo user;
}
