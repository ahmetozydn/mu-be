package com.mulakatim.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query(value = """
            SELECT id FROM questions
            WHERE category_id = :categoryId
              AND UPPER(language) = UPPER(:language)
              AND active = true
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findRandomIds(@Param("categoryId") String categoryId,
                               @Param("language") String language,
                               @Param("limit") int limit);

    @Query(value = """
            SELECT id FROM questions
            WHERE category_id = :categoryId
              AND UPPER(language) = UPPER(:language)
              AND UPPER(difficulty) = UPPER(:difficulty)
              AND active = true
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findRandomIds(@Param("categoryId") String categoryId,
                               @Param("language") String language,
                               @Param("difficulty") String difficulty,
                               @Param("limit") int limit);

    @Query(value = """
            SELECT id FROM questions
            WHERE category_id = :categoryId
              AND UPPER(language) = UPPER(:language)
              AND active = true
              AND id NOT IN (:excludeIds)
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findRandomIdsExcluding(@Param("categoryId") String categoryId,
                                        @Param("language") String language,
                                        @Param("limit") int limit,
                                        @Param("excludeIds") List<UUID> excludeIds);

    @Query(value = """
            SELECT id FROM questions
            WHERE category_id = :categoryId
              AND UPPER(language) = UPPER(:language)
              AND UPPER(difficulty) = UPPER(:difficulty)
              AND active = true
              AND id NOT IN (:excludeIds)
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findRandomIdsExcluding(@Param("categoryId") String categoryId,
                                        @Param("language") String language,
                                        @Param("difficulty") String difficulty,
                                        @Param("limit") int limit,
                                        @Param("excludeIds") List<UUID> excludeIds);
}
