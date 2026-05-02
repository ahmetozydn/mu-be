package com.mulakatim.domain.quiz.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(@NotNull Integer selectedIndex) {
}
