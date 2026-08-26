package com.careerthon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

/**
 * Keeps the Render free-tier instance alive by pinging itself every 10 minutes.
 * Render spins down free services after 15 minutes of inactivity, causing
 * slow cold-starts and OOM crashes. Self-pinging prevents this.
 *
 * Enabled only when RENDER=true env var is set (automatically set by Render).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "keep.alive.enabled", havingValue = "true", matchIfMissing = false)
public class KeepAliveScheduler {

    @Value("${server.port:8080}")
    private int port;

    @Value("${keep.alive.url:}")
    private String keepAliveUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Pings /health every 10 minutes to prevent Render from sleeping.
     * initialDelay=180s ensures the app is fully up before first ping.
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 180_000)
    public void keepAlive() {
        String url = keepAliveUrl.isEmpty()
                ? "http://localhost:" + port + "/health"
                : keepAliveUrl + "/health";
        try {
            restTemplate.getForObject(url, String.class);
            System.out.println("✅ Keep-alive ping successful: " + url);
        } catch (Exception e) {
            System.err.println("⚠️ Keep-alive ping failed (non-fatal): " + e.getMessage());
        }
    }
}
