package com.mulakatim.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {

    boolean existsBySessionIdAndPosition(UUID sessionId, short position);

    int countBySessionIdAndCorrectTrue(UUID sessionId);

    List<QuizAnswer> findBySessionId(UUID sessionId);
}
