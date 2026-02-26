package com.quizqueens.dto;

import com.quizqueens.entities.Quiz;
import com.quizqueens.entities.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuizRequest {
    @NotBlank
    private String title;
    private String description;
    private Quiz.QuizType quizType = Quiz.QuizType.MCQ;
    private List<QuestionRequest> questions;

    @Data
    public static class QuestionRequest {
        @NotBlank
        private String questionText;
        private Question.QuestionType questionType;
        private List<String> options;
        private String correctAnswer;
    }
}

@Data
class QuizSummaryResponse {
    private Long id;
    private String title;
    private String description;
    private String quizType;
    private String creatorUsername;
    private int questionCount;
    private LocalDateTime createdAt;
}