package com.mulakatim.domain.quiz.dto;

import java.util.List;

public record AnswerDetailDto(
        int position,
        String questionText,
        List<String> options,
        int selectedIndex,
        int correctIndex,
        boolean correct,
        String explanation
) {
}
