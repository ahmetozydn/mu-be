package com.mulakatim.domain.category;

import com.mulakatim.domain.category.dto.CategoryResponse;
import com.mulakatim.domain.category.dto.QuestionCountDto;
import com.mulakatim.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public List<CategoryResponse> getAllActive(String lang) {
        String cacheKey = lang.toLowerCase();
        CacheEntry entry = cache.get(cacheKey);

        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }

        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        Map<String, QuestionCountDto> counts = getQuestionCounts();

        List<CategoryResponse> result = categories.stream()
                .map(c -> CategoryResponse.from(c, lang, counts.getOrDefault(c.getId(), QuestionCountDto.empty())))
                .collect(Collectors.toList());

        cache.put(cacheKey, new CacheEntry(result));
        return result;
    }

    public CategoryResponse getById(String id, String lang) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> ApiException.notFound("CATEGORY_NOT_FOUND", "Kategori bulunamadi."));

        Map<String, QuestionCountDto> counts = getQuestionCounts();
        return CategoryResponse.from(category, lang, counts.getOrDefault(id, QuestionCountDto.empty()));
    }

    public void evictCache() {
        cache.clear();
    }

    private Map<String, QuestionCountDto> getQuestionCounts() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT category_id, difficulty, COUNT(*) as cnt " +
                    "FROM questions WHERE active = true " +
                    "GROUP BY category_id, difficulty"
            );

            return rows.stream().collect(
                    Collectors.groupingBy(
                            row -> (String) row.get("category_id"),
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> {
                                        long junior = 0, mid = 0, senior = 0;
                                        for (Map<String, Object> row : list) {
                                            String diff = ((String) row.get("difficulty")).toLowerCase();
                                            long cnt = ((Number) row.get("cnt")).longValue();
                                            switch (diff) {
                                                case "junior" -> junior += cnt;
                                                case "mid" -> mid += cnt;
                                                case "senior" -> senior += cnt;
                                            }
                                        }
                                        return new QuestionCountDto(junior, mid, senior, junior + mid + senior);
                                    }
                            )
                    )
            );
        } catch (Exception e) {
            log.debug("Soru sayisi cekilemedi: {}", e.getMessage());
            return Map.of();
        }
    }

    private static class CacheEntry {
        final List<CategoryResponse> value;
        final Instant expiresAt;

        CacheEntry(List<CategoryResponse> value) {
            this.value = value;
            this.expiresAt = Instant.now().plus(CACHE_TTL);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
