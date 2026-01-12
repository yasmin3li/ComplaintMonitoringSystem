package com.myapp.complaints.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myapp.complaints.enums.TokenType;
import lombok.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("access_token_expiry")
    private String accessTokenExpiry;

    @JsonProperty("token_type")
    private TokenType tokenType;

    @JsonProperty("user_name")
    private String userName;

//    @JsonProperty("user_age")
//    private int userAge=12;
}