package com.careerthon.repository;

import com.careerthon.model.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    Optional<Internship> findByCourseId(Long courseId);
}
