package com.quizqueens.repository;

import com.quizqueens.entities.Quiz;
import com.quizqueens.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreatorOrderByCreatedAtDesc(User creator);

    @Query("SELECT q FROM Quiz q ORDER BY q.createdAt DESC")
    List<Quiz> findAllOrderByCreatedAtDesc();
}