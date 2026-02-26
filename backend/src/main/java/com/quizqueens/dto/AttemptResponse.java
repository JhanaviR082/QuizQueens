package com.quizqueens.dto;

import lombok.Data;
import java.util.List;

@Data
public class AttemptResponse {
    private int score;
    private int total;
    private List<BreakdownItem> breakdown;

    @Data
    public static class BreakdownItem {
        private Long questionId;
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
    }
}
