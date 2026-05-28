package com.kizuna.data_service.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonAuthHandlers {

    private JsonAuthHandlers() {
    }

    public static AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) ->
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized",
                        "Autenticação necessária. Envie um token Bearer válido.");
    }

    public static AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) ->
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "forbidden",
                        "Sem permissão para este recurso. Role ADMIN é necessária.");
    }

    private static void writeJson(
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}",
                escape(error),
                escape(message)
        );
        response.getWriter().write(body);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
