package com.mulakatim.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record StartQuizRequest(
        String categoryId,
        List<String> categoryIds,
        @NotBlank String language,
        String difficulty,
        boolean isKarma,
        Integer questionLimit
) {
}
