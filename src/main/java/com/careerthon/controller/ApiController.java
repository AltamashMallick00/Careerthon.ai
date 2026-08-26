package com.careerthon.controller;

import com.careerthon.model.ProfileReview;
import com.careerthon.service.ProfileAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProfileAnalyzerService analyzerService;

    public ApiController(ProfileAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ProfileReview>> getAllReviews() {
        return ResponseEntity.ok(analyzerService.getAllReviews());
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<ProfileReview> getReview(@PathVariable Long id) {
        Optional<ProfileReview> review = analyzerService.getReview(id);
        return review.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reviews/{id}/status")
    public ResponseEntity<String> getStatus(@PathVariable Long id) {
        Optional<ProfileReview> review = analyzerService.getReview(id);
        if (review.isPresent()) {
            return ResponseEntity.ok("{\"status\":\"" + review.get().getStatus() + "\"}");
        }
        return ResponseEntity.notFound().build();
    }
}
