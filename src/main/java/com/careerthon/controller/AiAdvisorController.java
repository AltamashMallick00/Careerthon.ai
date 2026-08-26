package com.careerthon.controller;

import com.careerthon.service.LlmService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAdvisorController {

    private final LlmService llmService;

    public AiAdvisorController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/generate")
    public Map<String, String> generateAdvice(@RequestBody Map<String, String> request) {
        String toolName = request.getOrDefault("tool", "General Advisor");
        String input = request.getOrDefault("input", "");
        
        String result = llmService.generateResponse(toolName, input);
        
        return Map.of(
            "status", "success",
            "result", result,
            "tool", toolName
        );
    }
}
