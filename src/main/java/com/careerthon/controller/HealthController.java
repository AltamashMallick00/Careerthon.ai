package com.careerthon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight health/keep-alive endpoint.
 * Ping this every 10 minutes via UptimeRobot (free) to prevent
 * Render free tier from spinning the service down.
 *
 * URL: GET /health  → returns {"status":"ok"}
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "app", "Careerthon.AI"));
    }
}
