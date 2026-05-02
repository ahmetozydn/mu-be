package com.mulakatim.domain.quiz.dto;

import java.util.List;
import java.util.UUID;

public record QuestionDto(UUID id, String text, List<String> options) {
}
