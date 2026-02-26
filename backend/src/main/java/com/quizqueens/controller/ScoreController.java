package com.quizqueens.controller;

import com.quizqueens.entities.*;
import com.quizqueens.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreRepository scoreRepo;
    private final UserRepository userRepo;

    public ScoreController(ScoreRepository scoreRepo, UserRepository userRepo) {
        this.scoreRepo = scoreRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Map<String, Object>>> getMyScores(
            @AuthenticationPrincipal UserDetails principal) {

        User user = userRepo.findByUsername(principal.getUsername()).orElseThrow();
        List<Score> scores = scoreRepo.findByUserOrderByAttemptedAtDesc(user);

        List<Map<String, Object>> response = scores.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("quizTitle", s.getQuiz().getTitle());
            map.put("quizId", s.getQuiz().getId());
            map.put("score", s.getScore());
            map.put("total", s.getTotal());
            map.put("attemptedAt", s.getAttemptedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}