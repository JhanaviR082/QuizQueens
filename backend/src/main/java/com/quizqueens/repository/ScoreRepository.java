package com.quizqueens.repository;

import com.quizqueens.entities.Score;
import com.quizqueens.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByUserOrderByAttemptedAtDesc(User user);
}