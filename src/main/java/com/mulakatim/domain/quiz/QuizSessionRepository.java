package com.mulakatim.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {
}
