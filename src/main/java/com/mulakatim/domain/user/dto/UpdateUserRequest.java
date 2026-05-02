package com.mulakatim.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Ad bos olamaz.")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir.")
        String name
) {
}
