package com.careerthon.repository;

import com.careerthon.model.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByStudentId(Long studentId);
    QuizSubmission findByQuizIdAndStudentId(Long quizId, Long studentId);
}
