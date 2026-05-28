package com.kizuna.data_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KizunaJwtRoleResolverTest {

    @Test
    void resolvesRealmAndClientRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("default-roles-kizuna", "ADMIN")))
                .claim("resource_access", Map.of(
                        "kizuna-app", Map.of("roles", List.of("uma_protection"))
                ))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThat(KizunaJwtRoleResolver.hasAdminAccess(jwt)).isTrue();
        assertThat(KizunaJwtRoleResolver.resolveRoles(jwt))
                .contains("ADMIN", "DEFAULT-ROLES-KIZUNA");
    }

    @Test
    void resolvesAdminFromClientRolesOnly() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("resource_access", Map.of(
                        "kizuna-app", Map.of("roles", List.of("ADMIN"))
                ))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThat(KizunaJwtRoleResolver.hasAdminAccess(jwt)).isTrue();
    }
}
