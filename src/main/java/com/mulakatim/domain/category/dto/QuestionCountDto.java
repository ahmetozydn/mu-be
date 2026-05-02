package com.mulakatim.domain.category.dto;

public record QuestionCountDto(
        long junior,
        long mid,
        long senior,
        long total
) {
    public static QuestionCountDto empty() {
        return new QuestionCountDto(0, 0, 0, 0);
    }
}
