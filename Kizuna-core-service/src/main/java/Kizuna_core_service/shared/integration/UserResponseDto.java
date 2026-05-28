package Kizuna_core_service.shared.integration;

import java.util.List;

public record UserResponseDto(String keycloakId, String username, String fullName, String email, List<String> roles) {
}
