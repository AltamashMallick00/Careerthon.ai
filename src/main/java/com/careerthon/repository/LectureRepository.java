package com.careerthon.repository;

import com.careerthon.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByCourseIdOrderByModuleOrderAscLectureOrderAsc(Long courseId);
    long countByCourseId(Long courseId);
}
