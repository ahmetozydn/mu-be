package com.mulakatim.domain.category.dto;

import com.mulakatim.domain.category.Category;
import com.mulakatim.shared.enums.CategoryType;

public record CategoryResponse(
        String id,
        CategoryType type,
        String title,
        String description,
        String icon,
        QuestionCountDto questionCount
) {
    public static CategoryResponse from(Category category, String lang, QuestionCountDto questionCount) {
        boolean tr = "tr".equalsIgnoreCase(lang);
        return new CategoryResponse(
                category.getId(),
                category.getType(),
                tr ? category.getTitleTr() : category.getTitleEn(),
                tr ? category.getDescriptionTr() : category.getDescriptionEn(),
                category.getIcon(),
                questionCount
        );
    }
}
