package Kizuna_core_service.shared.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SecurityUtils {

    public static String getUsername() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
    }

    public static String getUserId() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getSubject() : null;
    }


    public static List<String> getRoles() {
        Jwt jwt = getJwt();

        if (jwt == null) {
            return Collections.emptyList();
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return Collections.emptyList();
        }

        return (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
    }

    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    private static Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt;
        }

        return null;
    }
}