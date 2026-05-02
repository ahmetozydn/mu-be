package com.mulakatim.domain.quiz;

import com.mulakatim.shared.enums.QuizStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(nullable = false, length = 5)
    private String language;

    @Column(length = 10)
    private String difficulty;

    @Column(name = "is_karma", nullable = false)
    @Builder.Default
    private boolean karma = false;

    @Column(name = "is_guest", nullable = false)
    @Builder.Default
    private boolean guest = false;

    @Column(name = "current_index", nullable = false)
    @Builder.Default
    private short currentIndex = 0;

    private Short score;

    @Column(name = "total_questions", nullable = false)
    private short totalQuestions;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QuizStatus status = QuizStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
