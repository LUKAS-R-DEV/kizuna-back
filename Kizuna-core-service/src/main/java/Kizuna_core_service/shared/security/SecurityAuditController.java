package Kizuna_core_service.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
public class SecurityAuditController {

    private final SecurityAuditService securityAuditService;

    @PostMapping("/access-denied")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportAccessDenied(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {
        String path = body != null ? body.getOrDefault("path", request.getRequestURI()) : request.getRequestURI();
        String method = body != null ? body.getOrDefault("method", "UNKNOWN") : "UNKNOWN";
        String service = body != null ? body.getOrDefault("service", "frontend") : "frontend";
        String reason = body != null ? body.getOrDefault("reason", "FRONTEND_403") : "FRONTEND_403";

        String username = body != null ? body.get("username") : null;
        String userId = body != null ? body.get("userId") : null;
        String roles = body != null ? body.get("roles") : null;

        securityAuditService.logAccessDenied(
                method,
                path,
                service,
                reason,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                username,
                userId,
                roles
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
