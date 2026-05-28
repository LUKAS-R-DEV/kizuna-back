package kizuna_iam_service.dto;

import java.util.List;

public record UserResponseDto(String keycloakId, String username, String fullName, String email, List<String> roles) {
}
