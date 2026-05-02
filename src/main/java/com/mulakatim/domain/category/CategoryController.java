package com.mulakatim.domain.category;

import com.mulakatim.domain.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(
            @RequestParam(defaultValue = "tr") String lang
    ) {
        return ResponseEntity.ok(categoryService.getAllActive(lang));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable String id,
            @RequestParam(defaultValue = "tr") String lang
    ) {
        return ResponseEntity.ok(categoryService.getById(id, lang));
    }
}
