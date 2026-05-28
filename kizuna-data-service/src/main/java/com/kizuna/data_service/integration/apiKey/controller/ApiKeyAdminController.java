package com.kizuna.data_service.integration.apiKey.controller;

import com.kizuna.data_service.config.KizunaJwtRoleResolver;
import com.kizuna.data_service.integration.apiKey.dto.ApiKeyRequestDto;
import com.kizuna.data_service.integration.apiKey.dto.ApiKeyResponseDto;
import com.kizuna.data_service.integration.apiKey.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/integration/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyAdminController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/auth-check")
    public Map<String, Object> authCheck(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("admin", KizunaJwtRoleResolver.hasAdminAccess(jwt));
        body.put("roles", KizunaJwtRoleResolver.resolveRoles(jwt));
        body.put("username", jwt != null ? jwt.getClaimAsString("preferred_username") : null);
        body.put("issuer", jwt != null && jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
        body.put("realm_access_claim", jwt != null ? jwt.getClaim("realm_access") : null);
        return body;
    }

    @GetMapping
    public List<ApiKeyResponseDto> list() {
        return apiKeyService.findAllMasked();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponseDto create(@Valid @RequestBody ApiKeyRequestDto request) {
        return apiKeyService.create(request);
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable String id) {
        apiKeyService.disable(id);
    }
}
