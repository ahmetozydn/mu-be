package com.mulakatim.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mulakatim.domain.user.dto.UserResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user,
        Boolean isNewUser    // sadece Google auth'ta kullanılır
) {
    public static AuthResponse of(String accessToken, String refreshToken, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, user, null);
    }

    public static AuthResponse ofGoogle(String accessToken, String refreshToken, UserResponse user, boolean isNewUser) {
        return new AuthResponse(accessToken, refreshToken, user, isNewUser);
    }
}
