package com.careerthon.repository;

import com.careerthon.model.InternshipTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipTaskRepository extends JpaRepository<InternshipTask, Long> {
    List<InternshipTask> findByInternshipIdOrderByDeadlineAsc(Long internshipId);
}
