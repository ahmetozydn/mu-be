package com.mulakatim.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "idToken bos olamaz.")
        String idToken
) {
}
