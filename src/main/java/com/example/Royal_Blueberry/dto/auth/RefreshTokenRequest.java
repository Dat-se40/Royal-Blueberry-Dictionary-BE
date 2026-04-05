package com.example.Royal_Blueberry.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class RefreshTokenRequest implements Serializable {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
