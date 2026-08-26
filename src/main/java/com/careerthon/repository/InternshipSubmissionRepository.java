package com.careerthon.repository;

import com.careerthon.model.InternshipSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipSubmissionRepository extends JpaRepository<InternshipSubmission, Long> {
    List<InternshipSubmission> findByTaskId(Long taskId);
    Optional<InternshipSubmission> findByTaskIdAndStudentId(Long taskId, Long studentId);
}
