package com.quizqueens.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizqueens.dto.*;
import com.quizqueens.entities.*;
import com.quizqueens.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizRepository quizRepo;
    private final UserRepository userRepo;
    private final ScoreRepository scoreRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizController(QuizRepository quizRepo, UserRepository userRepo, ScoreRepository scoreRepo) {
        this.quizRepo = quizRepo;
        this.userRepo = userRepo;
        this.scoreRepo = scoreRepo;
    }

    // ── GET ALL QUIZZES (public) ─────────────────────────────
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllQuizzes() {
        List<Quiz> quizzes = quizRepo.findAllOrderByCreatedAtDesc();
        return ResponseEntity.ok(quizzes.stream().map(this::toSummary).collect(Collectors.toList()));
    }

    // ── GET MY QUIZZES ──────────────────────────────────────
    @GetMapping("/mine")
    public ResponseEntity<List<Map<String, Object>>> getMyQuizzes(@AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(quizRepo.findByCreatorOrderByCreatedAtDesc(user)
                .stream().map(this::toSummary).collect(Collectors.toList()));
    }

    // ── GET QUIZ BY ID (public, no correct answers) ─────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable Long id) {
        return quizRepo.findById(id)
                .map(quiz -> ResponseEntity.ok(toDetail(quiz, false)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── CREATE QUIZ ──────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody CreateQuizRequest req,
                                        @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);

        if (req.getTitle() == null || req.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));
        }
        if (req.getQuestions() == null || req.getQuestions().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "At least one question required"));
        }

        Quiz quiz = Quiz.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .quizType(req.getQuizType() != null ? req.getQuizType() : Quiz.QuizType.MCQ)
                .creator(user)
                .questions(new ArrayList<>())
                .build();

        for (CreateQuizRequest.QuestionRequest qr : req.getQuestions()) {
            Question question = Question.builder()
                    .questionText(qr.getQuestionText())
                    .questionType(qr.getQuestionType() != null ? qr.getQuestionType() : Question.QuestionType.MCQ)
                    .correctAnswer(qr.getCorrectAnswer())
                    .quiz(quiz)
                    .build();

            // Store MCQ options as JSON
            if (qr.getOptions() != null && !qr.getOptions().isEmpty()) {
                try {
                    question.setOptions(objectMapper.writeValueAsString(qr.getOptions()));
                } catch (JsonProcessingException e) {
                    question.setOptions("[]");
                }
            }
            quiz.getQuestions().add(question);
        }

        Quiz saved = quizRepo.save(quiz);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "message", "Quiz created successfully"));
    }

    // ── DELETE QUIZ ──────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        return quizRepo.findById(id).map(quiz -> {
            if (!quiz.getCreator().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Not authorized"));
            }
            quizRepo.delete(quiz);
            return ResponseEntity.ok(Map.of("message", "Quiz deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── ATTEMPT QUIZ ─────────────────────────────────────────
    @PostMapping("/{id}/attempt")
    public ResponseEntity<?> attemptQuiz(@PathVariable Long id,
                                         @RequestBody AttemptRequest req,
                                         @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        Quiz quiz = quizRepo.findById(id).orElse(null);
        if (quiz == null) return ResponseEntity.notFound().build();

        // Build answer map
        Map<Long, String> answerMap = new HashMap<>();
        if (req.getAnswers() != null) {
            for (AttemptRequest.AnswerItem item : req.getAnswers()) {
                answerMap.put(item.getQuestionId(), item.getAnswer() != null ? item.getAnswer().trim() : "");
            }
        }

        int score = 0;
        List<AttemptResponse.BreakdownItem> breakdown = new ArrayList<>();

        for (Question q : quiz.getQuestions()) {
            String userAnswer = answerMap.getOrDefault(q.getId(), "");
            boolean correct = q.getCorrectAnswer().trim().equalsIgnoreCase(userAnswer);
            if (correct) score++;

            AttemptResponse.BreakdownItem item = new AttemptResponse.BreakdownItem();
            item.setQuestionId(q.getId());
            item.setQuestionText(q.getQuestionText());
            item.setUserAnswer(userAnswer);
            item.setCorrectAnswer(q.getCorrectAnswer());
            item.setCorrect(correct);
            breakdown.add(item);
        }

        int total = quiz.getQuestions().size();

        // Save score
        Score scoreEntity = Score.builder()
                .user(user)
                .quiz(quiz)
                .score(score)
                .total(total)
                .build();
        scoreRepo.save(scoreEntity);

        AttemptResponse response = new AttemptResponse();
        response.setScore(score);
        response.setTotal(total);
        response.setBreakdown(breakdown);

        return ResponseEntity.ok(response);
    }

    // ── HELPERS ──────────────────────────────────────────────

    private User getUser(UserDetails principal) {
        return userRepo.findByUsername(principal.getUsername()).orElseThrow();
    }

    private Map<String, Object> toSummary(Quiz quiz) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", quiz.getId());
        map.put("title", quiz.getTitle());
        map.put("description", quiz.getDescription());
        map.put("quizType", quiz.getQuizType().name());
        map.put("creatorUsername", quiz.getCreator().getUsername());
        map.put("questionCount", quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        map.put("createdAt", quiz.getCreatedAt());
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toDetail(Quiz quiz, boolean includeAnswers) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", quiz.getId());
        map.put("title", quiz.getTitle());
        map.put("description", quiz.getDescription());
        map.put("quizType", quiz.getQuizType().name());
        map.put("creatorUsername", quiz.getCreator().getUsername());

        List<Map<String, Object>> questions = new ArrayList<>();
        for (Question q : quiz.getQuestions()) {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("questionType", q.getQuestionType().name());

            // Parse options JSON
            List<String> options = new ArrayList<>();
            if (q.getOptions() != null && !q.getOptions().isBlank()) {
                try {
                    options = objectMapper.readValue(q.getOptions(), List.class);
                } catch (Exception ignored) {}
            }
            qMap.put("options", options);

            if (includeAnswers) qMap.put("correctAnswer", q.getCorrectAnswer());
            questions.add(qMap);
        }
        map.put("questions", questions);
        return map;
    }
}