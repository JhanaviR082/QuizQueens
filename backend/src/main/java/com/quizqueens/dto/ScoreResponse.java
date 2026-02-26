package com.quizqueens.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreResponse {
    private Long id;
    private String quizTitle;
    private int score;
    private int total;
    private LocalDateTime attemptedAt;
}
