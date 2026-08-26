package com.careerthon.repository;

import com.careerthon.model.LmsAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LmsAssignmentRepository extends JpaRepository<LmsAssignment, Long> {
    List<LmsAssignment> findByCourseIdOrderByDueDateAsc(Long courseId);
}
