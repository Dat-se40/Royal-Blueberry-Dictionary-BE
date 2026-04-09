package com.example.Royal_Blueberry.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class GoogleLoginRequest implements Serializable {

    @NotBlank(message = "Google authorization code is required")
    private String code;

    @NotBlank(message = "Google login state is required")
    private String state;
}
