package com.careerthon.controller;

import com.careerthon.model.User;
import com.careerthon.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@SuppressWarnings("null")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/{id}/toggle-block")
    public String toggleBlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Cannot block admin
            if (user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "Cannot block admin users.");
                return "redirect:/admin/users";
            }
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            String status = user.isEnabled() ? "unblocked" : "blocked";
            redirectAttributes.addFlashAttribute("message", "User " + user.getUsername() + " has been " + status + ".");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if (user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete admin users.");
                return "redirect:/admin/users";
            }
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("message", "User " + user.getUsername() + " deleted successfully.");
        }
        return "redirect:/admin/users";
    }
}
