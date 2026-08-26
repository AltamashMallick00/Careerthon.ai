package com.careerthon.controller;

import com.careerthon.model.Course;
import com.careerthon.model.Lecture;
import com.careerthon.repository.CourseRepository;
import com.careerthon.repository.LectureRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lms")
@SuppressWarnings("null")
public class LmsController {

    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;

    public LmsController(CourseRepository courseRepository, LectureRepository lectureRepository) {
        this.courseRepository = courseRepository;
        this.lectureRepository = lectureRepository;
    }

    @GetMapping({"", "/"})
    public String lmsDashboard(Model model) {
        List<Course> courses = courseRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("courses", courses);
        return "lms/dashboard";
    }

    @GetMapping("/course/{id}")
    public String courseDetail(@PathVariable Long id,
                               @RequestParam(required = false) Long lectureId,
                               Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            return "redirect:/lms";
        }

        List<Lecture> lectures = lectureRepository.findByCourseIdOrderByModuleOrderAscLectureOrderAsc(id);

        // Group lectures by module name (preserving order)
        Map<String, List<Lecture>> modules = lectures.stream()
                .collect(Collectors.groupingBy(Lecture::getModuleName, LinkedHashMap::new, Collectors.toList()));

        // Determine active lecture
        Lecture activeLecture = null;
        if (lectureId != null) {
            activeLecture = lectures.stream()
                    .filter(l -> l.getId().equals(lectureId))
                    .findFirst()
                    .orElse(null);
        }
        if (activeLecture == null && !lectures.isEmpty()) {
            activeLecture = lectures.get(0);
        }

        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("lectures", lectures);
        model.addAttribute("activeLecture", activeLecture);
        model.addAttribute("totalLectures", lectures.size());

        return "lms/course";
    }
}
