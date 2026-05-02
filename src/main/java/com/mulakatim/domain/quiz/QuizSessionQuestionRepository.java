package com.mulakatim.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizSessionQuestionRepository extends JpaRepository<QuizSessionQuestion, UUID> {

    Optional<QuizSessionQuestion> findBySessionIdAndPosition(UUID sessionId, short position);

    List<QuizSessionQuestion> findBySessionIdOrderByPosition(UUID sessionId);
}
