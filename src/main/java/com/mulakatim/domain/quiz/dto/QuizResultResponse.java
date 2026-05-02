package com.mulakatim.domain.quiz.dto;

import java.util.List;
import java.util.UUID;

public record QuizResultResponse(
        UUID sessionId,
        int score,
        int totalQuestions,
        String status,
        Integer durationSec,
        List<AnswerDetailDto> answers
) {
}
