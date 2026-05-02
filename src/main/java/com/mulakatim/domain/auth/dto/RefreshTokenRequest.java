package com.mulakatim.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken bos olamaz.")
        String refreshToken
) {
}
