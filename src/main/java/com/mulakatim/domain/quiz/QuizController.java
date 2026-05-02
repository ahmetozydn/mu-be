package com.mulakatim.domain.quiz;

import com.mulakatim.domain.quiz.dto.AnswerRequest;
import com.mulakatim.domain.quiz.dto.AnswerResponse;
import com.mulakatim.domain.quiz.dto.QuizResultResponse;
import com.mulakatim.domain.quiz.dto.StartQuizRequest;
import com.mulakatim.domain.quiz.dto.StartQuizResponse;
import com.mulakatim.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/start")
    public ResponseEntity<StartQuizResponse> start(
            @Valid @RequestBody StartQuizRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(quizService.startQuiz(request, user));
    }

    @PostMapping("/preview/start")
    public ResponseEntity<StartQuizResponse> previewStart(
            @Valid @RequestBody StartQuizRequest request
    ) {
        return ResponseEntity.ok(quizService.previewStart(request));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<QuizResultResponse> result(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(quizService.getResult(sessionId));
    }

    @GetMapping("/{sessionId}/current")
    public ResponseEntity<StartQuizResponse> current(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(quizService.getCurrentQuestion(sessionId));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResponse> answer(
            @PathVariable UUID sessionId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(quizService.submitAnswer(sessionId, request, user));
    }
}
