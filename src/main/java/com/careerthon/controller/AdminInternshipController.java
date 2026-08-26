package com.careerthon.controller;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/internship")
@SuppressWarnings("null")
public class AdminInternshipController {

    private final InternshipRepository internshipRepository;
    private final CourseRepository courseRepository;

    public AdminInternshipController(InternshipRepository internshipRepository, CourseRepository courseRepository) {
        this.internshipRepository = internshipRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/manage")
    public String manageInternships(Model model) {
        model.addAttribute("internships", internshipRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/lms/internship-manage";
    }

    @PostMapping("/create")
    public String createInternship(@RequestParam Long courseId, @RequestParam String title, 
                                   @RequestParam String description, @RequestParam String guidelinesPdfUrl, 
                                   RedirectAttributes redirectAttributes) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            Internship internship = new Internship(course, title, description, guidelinesPdfUrl);
            internshipRepository.save(internship);
            redirectAttributes.addFlashAttribute("message", "Internship created successfully!");
        }
        return "redirect:/admin/lms/internship/manage";
    }

    @PostMapping("/{id}/task/add")
    public String addTask(@PathVariable Long id, @RequestParam String title, @RequestParam String description, 
                          @RequestParam String deadline, RedirectAttributes redirectAttributes) {
        Internship internship = internshipRepository.findById(id).orElse(null);
        if (internship != null) {
            InternshipTask task = new InternshipTask(internship, title, description, LocalDateTime.parse(deadline));
            internship.getTasks().add(task);
            internshipRepository.save(internship);
            redirectAttributes.addFlashAttribute("message", "Task added successfully!");
        }
        return "redirect:/admin/lms/internship/manage";
    }
}
