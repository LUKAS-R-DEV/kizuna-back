package Kizuna_core_service.shared.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IamClient {

    private final RestTemplate restTemplate;
    private final String iamBaseUrl;

    public IamClient(
            RestTemplate restTemplate,
            @Value("${kizuna.iam.base-url:http://kizuna-iam-service:8083}") String iamBaseUrl) {
        this.restTemplate = restTemplate;
        this.iamBaseUrl = iamBaseUrl.endsWith("/") ? iamBaseUrl.substring(0, iamBaseUrl.length() - 1) : iamBaseUrl;
    }

    @CircuitBreaker(name = "iamService", fallbackMethod = "fallbackGetUserById")
    public UserResponseDto getUserById(String id) {

        String url = iamBaseUrl + "/users/{id}";

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String token = authentication.getToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserResponseDto.class,
                id
        );

        return response.getBody();
    }

    public UserResponseDto fallbackGetUserById(String id, Throwable ex) {
        throw new IllegalStateException("IAM indisponível para o usuário " + id + ": " + ex.getMessage(), ex);
    }
}