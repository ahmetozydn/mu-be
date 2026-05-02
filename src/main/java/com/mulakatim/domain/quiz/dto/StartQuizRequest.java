package com.mulakatim.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;

public record StartQuizRequest(
        @NotBlank String categoryId,
        @NotBlank String language,
        String difficulty,
        boolean isKarma
) {
}
