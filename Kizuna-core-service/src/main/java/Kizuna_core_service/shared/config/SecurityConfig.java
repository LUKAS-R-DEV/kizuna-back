package Kizuna_core_service.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import Kizuna_core_service.shared.security.AccessDeniedAuditHandler;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AccessDeniedAuditHandler accessDeniedAuditHandler,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/server-time").permitAll()
                        .requestMatchers("/security/**").authenticated()
                        .requestMatchers("/ws-production/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/recipes", "/recipes/**").hasAnyAuthority("ROLE_PLANNER", "ROLE_EXECUTIVE","ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/quality-inspection", "/quality-inspection/**").hasAnyAuthority("ROLE_INSPECTOR", "ROLE_EXECUTIVE","ROLE_ADMIN")
                        .requestMatchers("/production-order", "/production-order/**").hasAnyAuthority("ROLE_PLANNER", "ROLE_OPERATOR", "ROLE_EXECUTIVE","ROLE_INSPECTOR","ROLE_ADMIN")
                        .requestMatchers("/inventory", "/inventory/**", "/inventory-movement", "/inventory-movement/**").hasAnyAuthority("ROLE_INVENTORY_MANAGER","ROLE_EXECUTIVE","ROLE_PLANNER","ROLE_ADMIN")


                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedAuditHandler));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            // 1. Realm Roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                for (Object roleObj : roles) {
                    if (roleObj instanceof String role) {
                        String auth = "ROLE_" + role.toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(auth));
                    }
                }
            }
            // 2. Resource Roles
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((client, value) -> {
                    if (value instanceof Map<?, ?> clientMap) {
                        Object rolesObj = clientMap.get("roles");
                        if (rolesObj instanceof List<?> clientRoles) {
                            for (Object roleObj : clientRoles) {
                                if (roleObj instanceof String role) {
                                    String auth = "ROLE_" + role.toUpperCase();
                                    authorities.add(new SimpleGrantedAuthority(auth));
                                }
                            }
                        }
                    }
                });
            }

            return authorities;
        });

        return converter;
    }
}