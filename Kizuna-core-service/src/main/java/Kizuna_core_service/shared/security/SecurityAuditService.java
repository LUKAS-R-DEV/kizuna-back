package Kizuna_core_service.shared.security;

import Kizuna_core_service.shared.messaging.EventPublisher;
import Kizuna_core_service.shared.messaging.EventTopics;
import Kizuna_core_service.shared.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final EventPublisher eventPublisher;

    public void logAccessDenied(HttpServletRequest request, String reason) {
        logAccessDenied(
                request.getMethod(),
                request.getRequestURI(),
                "kizuna-core-service",
                reason,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }

    public void logAccessDenied(
            String method,
            String path,
            String service,
            String reason,
            String ip,
            String userAgent
    ) {
        logAccessDenied(method, path, service, reason, ip, userAgent, null, null, null);
    }

    public void logAccessDenied(
            String method,
            String path,
            String service,
            String reason,
            String ip,
            String userAgent,
            String usernameOverride,
            String userIdOverride,
            String rolesOverride
    ) {
        if (path != null && (path.contains("/api-ai") || path.contains("api-ai"))) {
            return;
        }

        String userId = firstNonBlank(userIdOverride, SecurityUtils.getUserId());
        String username = firstNonBlank(usernameOverride, SecurityUtils.getUsername());

        if (userId == null || userId.isBlank()) {
            userId = "ANONYMOUS";
        }
        if (username == null || username.isBlank()) {
            username = "ANONYMOUS";
        }

        String safeMethod = method != null ? method : "UNKNOWN";
        String safePath = path != null ? path : "/unknown";
        String entityId = safeMethod + ":" + safePath;

        String rolesCsv = rolesOverride != null && !rolesOverride.isBlank()
                ? rolesOverride
                : formatBusinessRoles(SecurityUtils.getRoles());

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("action", "ACCESS_DENIED");
        details.put("path", safePath);
        details.put("method", safeMethod);
        details.put("service", service != null ? service : "unknown");
        details.put("reason", reason != null ? reason : "Forbidden");
        details.put("roles", rolesCsv);
        details.put("ip", ip != null ? ip : "");
        details.put("userAgent", userAgent != null ? userAgent : "");

        eventPublisher.publish(
                EventTopics.AUDIT,
                "SECURITY",
                entityId,
                userId,
                username,
                details
        );
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private static String formatBusinessRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.stream()
                .filter(SecurityAuditService::isBusinessRole)
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private static boolean isBusinessRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String r = role.trim();
        return !r.startsWith("default-roles-")
                && !"offline_access".equalsIgnoreCase(r)
                && !"uma_authorization".equalsIgnoreCase(r);
    }
}
