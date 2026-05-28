package kizuna_notification_service.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RoleUtils {

    private static final Set<String> IGNORED_ROLES = Set.of(
            "OFFLINE_ACCESS",
            "UMA_AUTHORIZATION",
            "DEFAULT-ROLES-KIZUNA"
    );

    private RoleUtils() {
    }

    /** Normaliza para o nome da role de negócio (sem prefixo ROLE_). */
    public static String normalizeRoleName(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    public static boolean isBusinessRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = normalizeRoleName(role);
        return !normalized.startsWith("DEFAULT-ROLES-") && !IGNORED_ROLES.contains(normalized);
    }

    /** Gera variantes ROLE_X e X para consulta no MongoDB. */
    public static List<String> expandRoleAliases(List<String> roles) {
        Set<String> expanded = new LinkedHashSet<>();
        for (String role : roles) {
            if (!isBusinessRole(role)) {
                continue;
            }
            String normalized = normalizeRoleName(role);
            expanded.add(normalized);
            expanded.add("ROLE_" + normalized);
        }
        return new ArrayList<>(expanded);
    }
}
