package com.careerthon.repository;

import com.careerthon.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
    List<CourseProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<CourseProgress> findByStudentIdAndCourseIdAndLectureId(Long studentId, Long courseId, Long lectureId);
    boolean existsByStudentIdAndCourseIdAndLectureId(Long studentId, Long courseId, Long lectureId);
    long countByStudentIdAndCourseId(Long studentId, Long courseId);
}
