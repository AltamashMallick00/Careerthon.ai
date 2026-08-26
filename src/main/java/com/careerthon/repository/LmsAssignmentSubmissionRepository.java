package com.careerthon.repository;

import com.careerthon.model.LmsAssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LmsAssignmentSubmissionRepository extends JpaRepository<LmsAssignmentSubmission, Long> {
    List<LmsAssignmentSubmission> findByAssignmentId(Long assignmentId);
    Optional<LmsAssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}
