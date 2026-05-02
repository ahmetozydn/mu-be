package com.mulakatim.domain.quiz.dto;

public record AnswerResponse(
        boolean correct,
        int correctIndex,
        String explanation,
        int currentIndex,
        int totalQuestions,
        boolean completed,
        Integer score,
        QuestionDto nextQuestion
) {
}
