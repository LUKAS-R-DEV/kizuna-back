package kizuna_iam_service.service;

import jakarta.ws.rs.core.Response;
import kizuna_iam_service.dto.ApiResponseGeneric;
import kizuna_iam_service.dto.UserRequestDto;
import kizuna_iam_service.user.domain.User;
import kizuna_iam_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import kizuna_iam_service.dto.VerifyPasswordDto;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kizuna_iam_service.dto.UserResponseDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {
    private static final String REALM_NAME = "Kizuna";

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.verify-client-id:kizuna-app}")
    private String verifyClientId;

    @Value("${keycloak.public-base-url:http://localhost:8081}")
    private String keycloakPublicBaseUrl;

    @Value("${keycloak.app-client-id:kizuna-app}")
    private String appClientId;

    @Value("${keycloak.post-login-redirect:http://localhost/}")
    private String postLoginRedirect;

    public void createUserKizuna(UserRequestDto userRequestDto) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequestDto.username());
        user.setEmail(userRequestDto.email());
        user.setFirstName(userRequestDto.firstName());
        user.setLastName(userRequestDto.lastName() != null ? userRequestDto.lastName() : "");
        user.setEnabled(true);
        user.setEmailVerified(true);

        if (userRequestDto.password() != null && !userRequestDto.password().isBlank()) {
            CredentialRepresentation password = new CredentialRepresentation();
            password.setTemporary(false);
            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(userRequestDto.password());
            user.setCredentials(Collections.singletonList(password));
        }

        Response response = keycloak.realm(REALM_NAME).users().create(user);
        int status = response.getStatus();

        if (status == 201) {
            String userId = CreatedResponseUtil.getCreatedId(response);
            assignRoleToUser(userId, userRequestDto.role());
            if (userRequestDto.password() == null || userRequestDto.password().isBlank()) {
                try {
                    keycloak.realm(REALM_NAME)
                            .users()
                            .get(userId)
                            .executeActionsEmail(
                                    appClientId,
                                    postLoginRedirect,
                                    60 * 60 * 12,
                                    List.of("UPDATE_PASSWORD"));
                } catch (Exception e) {
                    log.warn("User {} created but password setup email failed: {}", userId, e.getMessage());
                }
            }
            response.close();
            return;
        }

        String errorBody = response.hasEntity() ? response.readEntity(String.class) : "No error body";
        response.close();
        throw new RuntimeException(
                "Keycloak creation failed with status: " + status + ". Details: " + errorBody
        );
    }

    public User getMe(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String fullname = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("email");

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        @SuppressWarnings("unchecked")
        List<String> roles = realmAccess != null
                ? (List<String>) realmAccess.get("roles")
                : List.of();

        User user = userRepository.findById(keycloakId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setFullName(fullname);
                    existing.setEmail(email);
                    existing.setRoles(roles);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKeycloakId(keycloakId);
                    newUser.setUsername(username);
                    newUser.setFullName(fullname);
                    newUser.setEmail(email);
                    newUser.setRoles(roles);
                    return userRepository.save(newUser);
                });

        return user;
    }

    public List<UserResponseDto> findAllUsers() {
        return keycloak.realm(REALM_NAME).users().list().stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }

    public UserRepresentation findById(String id) {
        return keycloak.realm(REALM_NAME).users().get(id).toRepresentation();
    }

    public UserResponseDto findUserResponseById(String id) {
        UserRepresentation rep = keycloak.realm(REALM_NAME).users().get(id).toRepresentation();
        return toUserResponseDto(rep);
    }

    public List<UserRepresentation> search(String query) {
        return keycloak.realm(REALM_NAME).users().search(query);
    }

    /**
     * Operadores com role OPERATOR no Keycloak (fonte de verdade).
     * O banco local pode estar incompleto se o usuário nunca chamou /users/me.
     */
    public List<UserResponseDto> findAllOperatorsFromKeycloak() {
        List<UserRepresentation> members;
        try {
            members = keycloak.realm(REALM_NAME).roles().get("OPERATOR").getUserMembers().stream().toList();
        } catch (Exception e) {
            log.warn("Role OPERATOR not found in realm, trying lowercase: {}", e.getMessage());
            members = keycloak.realm(REALM_NAME).roles().get("operator").getUserMembers().stream().toList();
        }

        if (members == null || members.isEmpty()) {
            return List.of();
        }

        return members.stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }

    public List<String> findAllRoles() {
        return keycloak.realm(REALM_NAME).roles().list().stream()
                .map(RoleRepresentation::getName)
                .filter(role -> !role.startsWith("default-roles-"))
                .filter(role -> !"offline_access".equals(role) && !"uma_authorization".equals(role))
                .sorted()
                .collect(Collectors.toList());
    }

    public ApiResponseGeneric update(String userId, UserRequestDto requestDto) {
        UserResource userResource = keycloak.realm(REALM_NAME).users().get(userId);
        UserRepresentation userRepresentation = userResource.toRepresentation();

        userRepresentation.setUsername(requestDto.username());
        userRepresentation.setFirstName(requestDto.firstName());
        userRepresentation.setLastName(requestDto.lastName() != null ? requestDto.lastName() : "");
        userRepresentation.setEmail(requestDto.email());
        userResource.update(userRepresentation);

        if (requestDto.role() != null && !requestDto.role().isBlank()) {
            syncUserRealmRole(userId, requestDto.role());
        }
        return apiResponseGeneric("User updated successfully");
    }

    public ApiResponseGeneric deleteById(String id) {
        keycloak.realm(REALM_NAME).users().get(id).remove();
        return apiResponseGeneric("User deleted successfully");
    }

    public boolean verifyPassword(VerifyPasswordDto verifyPasswordDto) {
        try {
            Keycloak keycloakVerify = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(REALM_NAME)
                    .clientId(verifyClientId)
                    .username(verifyPasswordDto.userName())
                    .password(verifyPasswordDto.password())
                    .grantType("password")
                    .build();
            keycloakVerify.tokenManager().getAccessToken();
            return true;
        } catch (Exception e) {
            log.debug("Password verification failed for {}: {}", verifyPasswordDto.userName(), e.getMessage());
            return false;
        }
    }

    private void assignRoleToUser(String userId, String roleName) {
        RoleRepresentation role = keycloak.realm(REALM_NAME).roles().get(roleName).toRepresentation();
        keycloak.realm(REALM_NAME).users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
    }

    private void syncUserRealmRole(String userId, String newRoleName) {
        var roleResource = keycloak.realm(REALM_NAME).users().get(userId).roles().realmLevel();
        List<RoleRepresentation> current = roleResource.listAll();
        List<RoleRepresentation> businessRoles = current.stream()
                .filter(r -> isBusinessRole(r.getName()))
                .toList();
        if (!businessRoles.isEmpty()) {
            roleResource.remove(businessRoles);
        }
        assignRoleToUser(userId, newRoleName);
    }

    private UserResponseDto toUserResponseDto(UserRepresentation rep) {
        String fullName = ((rep.getFirstName() != null ? rep.getFirstName() : "")
                + " "
                + (rep.getLastName() != null ? rep.getLastName() : "")).trim();

        return new UserResponseDto(
                rep.getId(),
                rep.getUsername(),
                fullName.isBlank() ? rep.getUsername() : fullName,
                rep.getEmail(),
                fetchBusinessRealmRoles(rep.getId())
        );
    }

    private List<String> fetchBusinessRealmRoles(String userId) {
        return keycloak.realm(REALM_NAME)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(UserManagementService::isBusinessRole)
                .sorted()
                .collect(Collectors.toList());
    }

    private static boolean isBusinessRole(String role) {
        return role != null
                && !role.startsWith("default-roles-")
                && !"offline_access".equals(role)
                && !"uma_authorization".equals(role);
    }

    private ApiResponseGeneric apiResponseGeneric(String message) {
        return new ApiResponseGeneric(message, LocalDateTime.now());
    }
}
