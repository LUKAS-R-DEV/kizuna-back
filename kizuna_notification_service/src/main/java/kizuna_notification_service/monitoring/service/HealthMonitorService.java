package kizuna_notification_service.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthMonitorService {

    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private final WebClient webClient;

    public Mono<Boolean> checkHealth(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException())
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .map(this::isUpBody)
                .onErrorResume(ex -> {
                    log.warn("[MONITOR] Health check failed for {}: {}", url, ex.getMessage());
                    return Mono.just(false);
                });
    }

    private boolean isUpBody(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        String normalized = body.toUpperCase();
        return normalized.contains("\"STATUS\":\"UP\"")
                || normalized.contains("\"STATUS\": \"UP\"")
                || normalized.contains("\"UP\"");
    }
}
