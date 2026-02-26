package com.quizqueens.dto;

import com.quizqueens.entities.Quiz;
import com.quizqueens.entities.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

// ── Auth DTOs ────────────────────────────────────────────
public class AuthDTOs {

    @Data
    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private UserInfo user;

        @Data
        public static class UserInfo {
            private Long id;
            private String username;
            private String email;
        }
    }
}

// ── Quiz DTOs ────────────────────────────────────────────
class QuizDTOs {

    @Data
    public static class CreateQuizRequest {
        @NotBlank private String title;
        private String description;
        private Quiz.QuizType quizType;
        private List<QuestionRequest> questions;
    }

    @Data
    public static class QuestionRequest {
        @NotBlank private String questionText;
        private Question.QuestionType questionType;
        private List<String> options;
        private String correctAnswer;
    }

    @Data
    public static class QuizSummaryResponse {
        private Long id;
        private String title;
        private String description;
        private String quizType;
        private String creatorUsername;
        private int questionCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class QuizDetailResponse {
        private Long id;
        private String title;
        private String description;
        private String quizType;
        private String creatorUsername;
        private List<QuestionResponse> questions;
    }

    @Data
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String questionType;
        private List<String> options;
        // correctAnswer NOT exposed in attempt view
    }
}