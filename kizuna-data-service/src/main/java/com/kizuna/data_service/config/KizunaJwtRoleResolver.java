package com.kizuna.data_service.config;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Extrai roles do JWT da mesma forma que o frontend (realm + clients + claims alternativos).
 */
public final class KizunaJwtRoleResolver {

    private KizunaJwtRoleResolver() {
    }

    public static Set<String> resolveRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        if (jwt == null) {
            return roles;
        }

        try {
            List<String> dottedRealmRoles = jwt.getClaimAsStringList("realm_access.roles");
            if (dottedRealmRoles != null) {
                addNames(roles, dottedRealmRoles);
            }
        } catch (Exception ignored) {
            // claim ausente ou formato inesperado
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            addNames(roles, realmAccess.get("roles"));
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, value) -> {
                if (value instanceof Map<?, ?> clientMap) {
                    addNames(roles, clientMap.get("roles"));
                }
            });
        }

        addNames(roles, jwt.getClaim("roles"));

        if (jwt.hasClaim("groups")) {
            roles.addAll(rolesFromGroups(jwt.getClaim("groups")));
        }

        return roles;
    }

    public static boolean hasAdminAccess(Jwt jwt) {
        return resolveRoles(jwt).stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role));
    }

    public static Collection<org.springframework.security.core.GrantedAuthority> toAuthorities(Jwt jwt) {
        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        for (String role : resolveRoles(jwt)) {
            String normalized = role.trim().toUpperCase(Locale.ROOT);
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(normalized));
        }
        return authorities;
    }

    private static Set<String> rolesFromGroups(Object groupsClaim) {
        Set<String> groups = new LinkedHashSet<>();
        if (groupsClaim instanceof Collection<?> collection) {
            for (Object group : collection) {
                if (group instanceof String groupName && !groupName.isBlank()) {
                    String normalized = groupName.startsWith("/")
                            ? groupName.substring(1)
                            : groupName;
                    groups.add(normalized.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        return groups;
    }

    private static void addNames(Set<String> target, Object rolesObject) {
        if (rolesObject == null) {
            return;
        }
        if (rolesObject instanceof Collection<?> collection) {
            for (Object roleObj : collection) {
                if (roleObj instanceof String role && !role.isBlank()) {
                    target.add(role.trim().toUpperCase(Locale.ROOT));
                }
            }
            return;
        }
        if (rolesObject instanceof String role && !role.isBlank()) {
            target.add(role.trim().toUpperCase(Locale.ROOT));
        }
    }
}
