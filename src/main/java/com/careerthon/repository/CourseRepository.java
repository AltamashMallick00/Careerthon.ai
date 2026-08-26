package com.careerthon.repository;

import com.careerthon.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByCreatedAtDesc();
    List<Course> findByCategoryIgnoreCase(String category);
}
