package com.careerthon.controller;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/lms/course")
@SuppressWarnings("null")
public class AdminCourseController {

    private final CourseRepository courseRepository;
    public AdminCourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/lms/course-form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course, RedirectAttributes redirectAttributes) {
        courseRepository.save(course);
        redirectAttributes.addFlashAttribute("message", "Course created successfully!");
        return "redirect:/admin/lms/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return "redirect:/admin/lms/courses";
        model.addAttribute("course", course);
        return "admin/lms/course-form";
    }

    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course courseDetails, RedirectAttributes redirectAttributes) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course != null) {
            course.setTitle(courseDetails.getTitle());
            course.setDescription(courseDetails.getDescription());
            course.setInstructor(courseDetails.getInstructor());
            course.setThumbnailUrl(courseDetails.getThumbnailUrl());
            course.setDifficulty(courseDetails.getDifficulty());
            course.setCategory(courseDetails.getCategory());
            course.setTotalDuration(courseDetails.getTotalDuration());
            course.setPrerequisites(courseDetails.getPrerequisites());
            course.setLearningOutcomes(courseDetails.getLearningOutcomes());
            courseRepository.save(course);
            redirectAttributes.addFlashAttribute("message", "Course updated successfully!");
        }
        return "redirect:/admin/lms/courses";
    }

    @GetMapping("/{id}/manage")
    public String manageCourse(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return "redirect:/admin/lms/courses";
        model.addAttribute("course", course);
        return "admin/lms/course-manage";
    }

    @PostMapping("/{id}/lesson/add")
    public String addLesson(@PathVariable Long id, @RequestParam String moduleName, @RequestParam String title, 
                            @RequestParam String youtubeVideoId, @RequestParam String duration, RedirectAttributes redirectAttributes) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course != null) {
            int moduleOrder = course.getLectures().stream()
                    .filter(l -> l.getModuleName().equals(moduleName))
                    .findFirst()
                    .map(Lecture::getModuleOrder)
                    .orElse(course.getLectures().size() + 1);
                    
            int lectureOrder = (int) course.getLectures().stream()
                    .filter(l -> l.getModuleName().equals(moduleName))
                    .count() + 1;
                    
            Lecture lecture = new Lecture(title, "", youtubeVideoId, moduleName, moduleOrder, lectureOrder, duration);
            course.addLecture(lecture);
            course.setTotalLectures(course.getTotalLectures() + 1);
            courseRepository.save(course);
            redirectAttributes.addFlashAttribute("message", "Lesson added successfully!");
        }
        return "redirect:/admin/lms/course/" + id + "/manage";
    }
}
