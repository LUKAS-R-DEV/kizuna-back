package kizuna_iam_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Password confirmation: no JWT filter (avoids 403 when planner token hits /users/** rules).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenticateChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .securityMatcher("/users/authenticate")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/allOperators").hasAnyAuthority(
                                "ROLE_PLANNER", "ROLE_OPERATOR", "ROLE_EXECUTIVE", "ROLE_INSPECTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/roles").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGE-USERS", "ROLE_manage-users")
                        .requestMatchers(HttpMethod.GET, "/users/*").hasAnyAuthority(
                                "ROLE_PLANNER", "ROLE_OPERATOR", "ROLE_EXECUTIVE", "ROLE_INSPECTOR", "ROLE_ADMIN")
                        .requestMatchers("/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGE-USERS")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> writeJsonError(
                                response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeJsonError(
                                response, HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                );

        return http.build();
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"status\":" + status + ",\"error\":\"" + message + "\",\"message\":\"" + message + "\"}";
        response.getWriter().write(body);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                for (Object roleObj : roles) {
                    if (roleObj instanceof String role) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey("realm-management")) {
                Map<String, Object> realmMgmt = (Map<String, Object>) resourceAccess.get("realm-management");
                if (realmMgmt.get("roles") instanceof List<?> mgmtRoles) {
                    for (Object roleObj : mgmtRoles) {
                        if (roleObj instanceof String role) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        }
                    }
                }
            }

            return authorities;
        });

        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${keycloak.auth-server-url}") String keycloakUrl) {
        return NimbusJwtDecoder.withJwkSetUri(
                keycloakUrl + "/realms/Kizuna/protocol/openid-connect/certs"
        ).build();
    }
}
