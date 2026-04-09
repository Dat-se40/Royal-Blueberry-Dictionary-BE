package com.example.Royal_Blueberry.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class GoogleLoginUrlResponse implements Serializable {

    private String url;

    private String state;

    private String redirectUri;

    private String scope;
}
