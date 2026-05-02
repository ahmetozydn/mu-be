package com.mulakatim.domain.category;

import com.mulakatim.shared.enums.CategoryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Column(name = "slug_tr", nullable = false, unique = true, length = 100)
    private String slugTr;

    @Column(name = "slug_en", nullable = false, unique = true, length = 100)
    private String slugEn;

    @Column(name = "title_tr", nullable = false, length = 100)
    private String titleTr;

    @Column(name = "title_en", nullable = false, length = 100)
    private String titleEn;

    @Column(name = "description_tr", columnDefinition = "TEXT")
    private String descriptionTr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
