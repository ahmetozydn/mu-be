package com.mulakatim.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ad bos olamaz.")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir.")
        String name,

        @NotBlank(message = "E-posta bos olamaz.")
        @Email(message = "Gecerli bir e-posta adresi girin.")
        String email,

        @NotBlank(message = "Sifre bos olamaz.")
        @Size(min = 8, max = 100, message = "Sifre en az 8, en fazla 100 karakter olabilir.")
        String password
) {
}
