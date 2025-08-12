package by.rublevskaya.authservice.controller;

import by.rublevskaya.authservice.dto.PartialUserRequest;
import by.rublevskaya.authservice.dto.UserRequest;
import by.rublevskaya.authservice.dto.UserResponse;
import by.rublevskaya.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        log.info("Received request to register new user with username '{}'", request.getUsername());
        UserResponse response = userService.createUser(request);
        log.info("User with username '{}' successfully registered", response.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Received request to fetch all users");
        List<UserResponse> users = userService.getAllUsers();
        log.info("Fetched {} user(s) successfully", users.size());
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        log.info("Received request to delete user with ID '{}'", id);
        validateOwnership(id, authentication);
        String resultMessage = userService.deleteUser(id);
        log.info("User with ID '{}' successfully deleted", id);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication
    ) {
        log.info("Received request to fetch user with ID '{}'", id);
        validateOwnership(id, authentication);
        UserResponse userResponse = userService.getUserById(id);
        log.info("Fetched user details for ID '{}': {}", id, userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request, Authentication authentication) {
        log.info("Received request to fully update user with ID '{}'", id);
        validateOwnership(id, authentication);
        UserResponse updatedUser = userService.updateUser(id, request);
        log.info("User with ID '{}' successfully updated", id);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> partiallyUpdateUser(@PathVariable Long id, @RequestBody PartialUserRequest request, Authentication authentication) {
        log.info("Received request to partially update user with ID '{}'", id);
        validateOwnership(id, authentication);
        UserResponse updatedUser = userService.partiallyUpdateUser(id, request);
        log.info("User with ID '{}' successfully partially updated", id);
        return ResponseEntity.ok(updatedUser);
    }

    private void validateOwnership(Long id, Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);
        if (!id.equals(userId)) {
            log.warn("Access denied for user '{}', attempting to access data for user ID '{}'", username, id);
            throw new AccessDeniedException("You don't have permission to access this resource.");
        }
    }
}