package com.mulakatim.domain.quiz.dto;

import java.util.UUID;

public record StartQuizResponse(
        UUID sessionId,
        int totalQuestions,
        int currentIndex,
        QuestionDto question
) {
}
