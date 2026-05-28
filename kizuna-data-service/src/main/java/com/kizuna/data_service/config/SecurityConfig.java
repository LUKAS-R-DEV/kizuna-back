package com.kizuna.data_service.config;

import com.kizuna.data_service.integration.apiKey.service.ApiKeyService;
import com.kizuna.data_service.integration.config.IntegrationProperties;
import com.kizuna.data_service.integration.security.IntegrationApiKeyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    IntegrationApiKeyFilter integrationApiKeyFilter(
            ApiKeyService apiKeyService,
            IntegrationProperties properties
    ) {
        return new IntegrationApiKeyFilter(apiKeyService, properties);
    }

    @Bean
    @Order(1)
    SecurityFilterChain integrationReadChain(
            HttpSecurity http,
            IntegrationApiKeyFilter integrationApiKeyFilter
    ) throws Exception {
        http.securityMatcher("/integration/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("INTEGRATION_READER"))
                .addFilterBefore(integrationApiKeyFilter, AnonymousAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain applicationChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(JsonAuthHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(JsonAuthHandlers.accessDeniedHandler()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(JsonAuthHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(JsonAuthHandlers.accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/integration/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/dashboard/**", "/report/**").authenticated()
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        // Valida assinatura + expiração; não exige issuer hostname (token vem com http://localhost)
        decoder.setJwtValidator(JwtValidators.createDefault());
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(KizunaJwtRoleResolver::toAuthorities);
        return converter;
    }
}
