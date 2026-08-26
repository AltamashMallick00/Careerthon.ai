package com.careerthon.repository;

import com.careerthon.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findAllByOrderByCreatedAtDesc();
}
