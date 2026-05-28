package kizuna_iam_service.user.controller;

import kizuna_iam_service.dto.ApiResponseGeneric;
import kizuna_iam_service.dto.UserRequestDto;
import kizuna_iam_service.dto.UserResponseDto;
import kizuna_iam_service.dto.VerifyPasswordDto;
import kizuna_iam_service.exception.NotFoundException;
import kizuna_iam_service.service.UserManagementService;
import kizuna_iam_service.user.domain.User;
import kizuna_iam_service.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserManagementService userService;


    @GetMapping("/me")
   public ResponseEntity<User> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getMe(jwt));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(userService.findAllRoles());
    }

    @GetMapping("/allOperators")
    public ResponseEntity<List<UserResponseDto>> getAllOperators() {
        return ResponseEntity.ok(userService.findAllOperatorsFromKeycloak());
    }

    @GetMapping()
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findUserResponseById(id));
    }
    @PutMapping("{id}")
    public ResponseEntity<ApiResponseGeneric> updateUser(@PathVariable String id, @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.update(id, userRequestDto));
    }
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponseGeneric> deleteUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.deleteById(id));
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponseGeneric> createUser(@RequestBody UserRequestDto requestDto) {
        try {
            userService.createUserKizuna(requestDto);
            return ResponseEntity.ok(new ApiResponseGeneric("User created successfully", java.time.LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponseGeneric("Failed to create user: " + e.getMessage(), java.time.LocalDateTime.now())
            );
        }
    }

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseGeneric> authenticate(@RequestBody VerifyPasswordDto verifyPasswordDto) {
        if (!userService.verifyPassword(verifyPasswordDto)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponseGeneric("Invalid username or password", java.time.LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(new ApiResponseGeneric("User authenticated successfully", java.time.LocalDateTime.now()));
    }

    private final UserResponseDto userResponseDto(User user) {
        return new UserResponseDto(user.getKeycloakId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRoles());
    }


}