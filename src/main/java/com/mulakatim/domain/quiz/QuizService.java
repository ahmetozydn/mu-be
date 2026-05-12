package com.mulakatim.domain.quiz;

import com.mulakatim.domain.category.CategoryRepository;
import com.mulakatim.domain.question.Question;
import com.mulakatim.domain.question.QuestionRepository;
import com.mulakatim.domain.quiz.dto.*;
import com.mulakatim.domain.user.User;
import com.mulakatim.shared.enums.QuizStatus;
import com.mulakatim.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final QuizSessionRepository sessionRepository;
    private final QuizSessionQuestionRepository sessionQuestionRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    @Value("${quiz.preview-limit:3}")
    private int previewLimit;

    @Value("${quiz.guest-session-ttl:3600}")
    private long guestSessionTtl;

    @Transactional
    public StartQuizResponse startQuiz(StartQuizRequest request, User user) {
        return createSession(request, user, DEFAULT_QUESTION_COUNT, user == null);
    }

    @Transactional
    public StartQuizResponse previewStart(StartQuizRequest request) {
        return createSession(request, null, previewLimit, true);
    }

    public QuizResultResponse getResult(UUID sessionId) {
        QuizSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Oturum bulunamadi."));

        List<QuizSessionQuestion> sessionQuestions = sessionQuestionRepository.findBySessionIdOrderByPosition(sessionId);

        Map<Short, QuizAnswer> answersByPosition = answerRepository.findBySessionId(sessionId)
                .stream().collect(Collectors.toMap(QuizAnswer::getPosition, a -> a));

        List<AnswerDetailDto> answers = sessionQuestions.stream().map(sq -> {
            Question q = questionRepository.findById(sq.getQuestionId()).orElseThrow();
            QuizAnswer answer = answersByPosition.get(sq.getPosition());
            List<String> options = q.getOptions().stream()
                    .sorted((a, b) -> Short.compare(a.getOptionIndex(), b.getOptionIndex()))
                    .map(o -> o.getOptionText())
                    .toList();
            return new AnswerDetailDto(
                    sq.getPosition(),
                    q.getQuestionText(),
                    options,
                    answer != null ? answer.getSelectedIndex() : -1,
                    q.getCorrectIndex(),
                    answer != null && answer.isCorrect(),
                    q.getExplanation()
            );
        }).toList();

        return new QuizResultResponse(
                session.getId(),
                session.getScore() != null ? session.getScore() : 0,
                session.getTotalQuestions(),
                session.getStatus().name(),
                session.getDurationSec(),
                answers
        );
    }

    public StartQuizResponse getCurrentQuestion(UUID sessionId) {
        QuizSession session = findActiveSession(sessionId);
        Question question = getQuestionAt(sessionId, session.getCurrentIndex());
        return new StartQuizResponse(session.getId(), session.getTotalQuestions(), session.getCurrentIndex(), toDto(question));
    }

    @Transactional
    public AnswerResponse submitAnswer(UUID sessionId, AnswerRequest request, User user) {
        QuizSession session = findActiveSession(sessionId);

        if (session.getUserId() != null && user != null && !session.getUserId().equals(user.getId())) {
            throw ApiException.forbidden("SESSION_FORBIDDEN", "Bu oturum size ait degil.");
        }

        short position = session.getCurrentIndex();

        if (answerRepository.existsBySessionIdAndPosition(sessionId, position)) {
            throw ApiException.badRequest("ALREADY_ANSWERED", "Bu soru zaten cevaplandi.");
        }

        Question question = getQuestionAt(sessionId, position);
        boolean correct = question.getCorrectIndex() == request.selectedIndex().shortValue();

        answerRepository.save(QuizAnswer.builder()
                .sessionId(sessionId)
                .questionId(question.getId())
                .position(position)
                .selectedIndex(request.selectedIndex().shortValue())
                .correct(correct)
                .build());

        short nextIndex = (short) (position + 1);
        session.setCurrentIndex(nextIndex);

        boolean completed = nextIndex >= session.getTotalQuestions();
        Integer score = null;
        QuestionDto nextQuestion = null;

        if (completed) {
            int correctCount = answerRepository.countBySessionIdAndCorrectTrue(sessionId);
            score = correctCount;
            session.setScore((short) correctCount);
            session.setStatus(QuizStatus.COMPLETED);
            session.setCompletedAt(Instant.now());
            session.setDurationSec((int) Duration.between(session.getStartedAt(), Instant.now()).getSeconds());
            log.info("Quiz completed: sessionId={} score={}/{}", sessionId, correctCount, session.getTotalQuestions());
        } else {
            nextQuestion = toDto(getQuestionAt(sessionId, nextIndex));
        }

        sessionRepository.save(session);

        return new AnswerResponse(
                correct,
                question.getCorrectIndex(),
                question.getExplanation(),
                nextIndex,
                session.getTotalQuestions(),
                completed,
                score,
                nextQuestion
        );
    }

    private StartQuizResponse createSession(StartQuizRequest request, User user, int limit, boolean guest) {
        categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> ApiException.notFound("CATEGORY_NOT_FOUND", "Kategori bulunamadi."));

        String lang = request.language().toUpperCase();
        String diff = request.difficulty() != null ? request.difficulty().toUpperCase() : null;

        List<UUID> questionIds = (diff != null
                ? questionRepository.findRandomIds(request.categoryId(), lang, diff, limit)
                : questionRepository.findRandomIds(request.categoryId(), lang, limit))
                .stream().map(UUID::fromString).toList();

        if (questionIds.isEmpty()) {
            throw ApiException.badRequest("NO_QUESTIONS", "Bu filtreler icin soru bulunamadi.");
        }

        Instant now = Instant.now();
        QuizSession session = QuizSession.builder()
                .userId(user != null ? user.getId() : null)
                .categoryId(request.categoryId())
                .language(lang)
                .difficulty(diff)
                .karma(request.isKarma())
                .guest(guest)
                .totalQuestions((short) questionIds.size())
                .startedAt(now)
                .expiresAt(guest ? now.plusSeconds(guestSessionTtl) : null)
                .build();

        sessionRepository.save(session);

        for (int i = 0; i < questionIds.size(); i++) {
            sessionQuestionRepository.save(QuizSessionQuestion.builder()
                    .sessionId(session.getId())
                    .questionId(questionIds.get(i))
                    .position((short) i)
                    .build());
        }

        Question first = questionRepository.findById(questionIds.get(0)).orElseThrow();
        return new StartQuizResponse(session.getId(), session.getTotalQuestions(), 0, toDto(first));
    }

    @Transactional
    public StartQuizResponse continueFromPreview(UUID sessionId, User user) {
        QuizSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Oturum bulunamadi."));

        if (!session.isGuest()) {
            throw ApiException.badRequest("ALREADY_UPGRADED", "Bu oturum zaten bir kullaniciya ait.");
        }

        // 1. Session'i guncelle
        session.setUserId(user.getId());
        session.setGuest(false);
        session.setStatus(QuizStatus.IN_PROGRESS);
        session.setExpiresAt(null);

        int currentTotal = session.getTotalQuestions();
        int targetTotal = DEFAULT_QUESTION_COUNT;
        int needed = targetTotal - currentTotal;

        if (needed > 0) {
            // 2. Mevcut sorulari bul (tekrar etmemesi icin)
            List<UUID> existingIds = sessionQuestionRepository.findBySessionIdOrderByPosition(sessionId)
                    .stream().map(QuizSessionQuestion::getQuestionId).toList();

            List<String> newIds;
            if (session.getDifficulty() != null) {
                newIds = questionRepository.findRandomIdsExcluding(
                        session.getCategoryId(),
                        session.getLanguage(),
                        session.getDifficulty(),
                        needed,
                        existingIds
                );
            } else {
                newIds = questionRepository.findRandomIdsExcluding(
                        session.getCategoryId(),
                        session.getLanguage(),
                        needed,
                        existingIds
                );
            }

            // 3. Yeni sorulari ekle
            for (int i = 0; i < newIds.size(); i++) {
                sessionQuestionRepository.save(QuizSessionQuestion.builder()
                        .sessionId(session.getId())
                        .questionId(UUID.fromString(newIds.get(i)))
                        .position((short) (currentTotal + i))
                        .build());
            }

            session.setTotalQuestions((short) (currentTotal + newIds.size()));
        }

        sessionRepository.save(session);

        // 4. Kalinan yerden devam et
        // Eger currentIndex == totalQuestions ise (yani 3 soruyu da bitirdiyse), 
        // currentIndex hala 3'tur ve yeni eklenen 4. soruya (index 3) isaret eder.
        Question nextQuestion = getQuestionAt(session.getId(), session.getCurrentIndex());
        return new StartQuizResponse(session.getId(), session.getTotalQuestions(), session.getCurrentIndex(), toDto(nextQuestion));
    }

    private QuizSession findActiveSession(UUID sessionId) {
        QuizSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Oturum bulunamadi."));

        if (session.getExpiresAt() != null && Instant.now().isAfter(session.getExpiresAt())) {
            session.setStatus(QuizStatus.EXPIRED);
            sessionRepository.save(session);
            throw ApiException.badRequest("SESSION_EXPIRED", "Oturum suresi doldu.");
        }

        if (session.getStatus() != QuizStatus.IN_PROGRESS) {
            throw ApiException.badRequest("SESSION_NOT_ACTIVE", "Oturum aktif degil.");
        }

        return session;
    }

    private Question getQuestionAt(UUID sessionId, short position) {
        UUID questionId = sessionQuestionRepository
                .findBySessionIdAndPosition(sessionId, position)
                .orElseThrow(() -> ApiException.notFound("QUESTION_NOT_FOUND", "Soru bulunamadi."))
                .getQuestionId();
        return questionRepository.findById(questionId)
                .orElseThrow(() -> ApiException.notFound("QUESTION_NOT_FOUND", "Soru bulunamadi."));
    }

    private QuestionDto toDto(Question q) {
        List<String> options = q.getOptions().stream()
                .sorted((a, b) -> Short.compare(a.getOptionIndex(), b.getOptionIndex()))
                .map(o -> o.getOptionText())
                .toList();
        return new QuestionDto(q.getId(), q.getQuestionText(), options);
    }
}
