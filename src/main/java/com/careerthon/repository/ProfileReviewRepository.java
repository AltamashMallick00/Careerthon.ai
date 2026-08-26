package com.careerthon.repository;

import com.careerthon.model.ProfileReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfileReviewRepository extends JpaRepository<ProfileReview, Long> {
    List<ProfileReview> findByStatusOrderByCreatedAtDesc(ProfileReview.ReviewStatus status);
    List<ProfileReview> findAllByOrderByCreatedAtDesc();
    List<ProfileReview> findByLinkedinUrlOrderByCreatedAtDesc(String linkedinUrl);

    // Pagination support
    Page<ProfileReview> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Search by name or LinkedIn URL (case-insensitive)
    Page<ProfileReview> findByUserNameContainingIgnoreCaseOrLinkedinUrlContainingIgnoreCaseOrEmailAddressContainingIgnoreCase(
            String name, String url, String email, Pageable pageable);

    // Average score for dashboard stats
    @Query("SELECT COALESCE(AVG(p.overallScore), 0) FROM ProfileReview p")
    double findAverageOverallScore();
}
