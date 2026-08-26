package com.careerthon.repository;

import com.careerthon.model.ResumeReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeReviewRepository extends JpaRepository<ResumeReview, Long> {
    List<ResumeReview> findByUserEmail(String userEmail);
    List<ResumeReview> findAllByOrderByUploadedAtDesc();

    // Pagination support
    Page<ResumeReview> findAllByOrderByUploadedAtDesc(Pageable pageable);

    // Search by name or email (case-insensitive)
    Page<ResumeReview> findByUserNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
            String name, String email, Pageable pageable);

    // Average ATS score for dashboard stats
    @Query("SELECT COALESCE(AVG(r.atsScore), 0) FROM ResumeReview r")
    double findAverageAtsScore();
}
