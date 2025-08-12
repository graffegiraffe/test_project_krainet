package by.rublevskaya.authservice.service;

import by.rublevskaya.authservice.clients.NotificationClient;
import by.rublevskaya.authservice.dto.NotificationRequest;
import by.rublevskaya.authservice.dto.PartialUserRequest;
import by.rublevskaya.authservice.dto.UserRequest;
import by.rublevskaya.authservice.dto.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import by.rublevskaya.authservice.exception.DataConflictException;
import by.rublevskaya.authservice.exception.UserNotFoundException;
import by.rublevskaya.authservice.model.Security;
import by.rublevskaya.authservice.model.User;
import by.rublevskaya.authservice.repository.SecurityRepository;
import by.rublevskaya.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final SecurityRepository securityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    @Value("${admin.email}")
    private String adminEmail;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("Attempting to create user with username '{}'", request.getUsername());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.error("Username '{}' already exists", request.getUsername());
            throw new DataConflictException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email '{}' already exists", request.getEmail());
            throw new DataConflictException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role("USER")
                .created_at(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Security security = new Security();
        security.setLogin(request.getUsername());
        security.setPassword(passwordEncoder.encode(request.getPassword()));
        security.setRole(user.getRole());
        security.setUserId(user.getId());
        securityRepository.save(security);

        log.info("User with ID '{}' created successfully", user.getId());

        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "A new user has been created",
                "User " + request.getUsername() + " was successfully created with email " + request.getEmail()
        ));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        log.info("Retrieving all users");
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Total users retrieved: {}", users.size());
        return users;
    }

    @Transactional
    public String deleteUser(Long id) {
        log.info("Attempting to delete user with Security ID '{}'", id);
        Security security = securityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Security entry with ID '{}' not found for deletion", id);
                    return new UserNotFoundException("User with Security ID " + id + " not found");
                });

        Long userId = security.getUserId();
        Optional<User> user = userRepository.findById(userId);

        securityRepository.deleteById(id);
        user.ifPresent(userRepository::delete);
        log.info("User with Security ID '{}' and User ID '{}' deleted successfully", id, userId);

        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User deleted",
                "User " + (user.isPresent() ? user.get().getUsername() : "[unknown]") +
                        " was removed from the system."
        ));
        return "User with Security ID " + id + " and User ID " + userId + " deleted successfully!";
    }

    private UserResponse mapToResponse(User user) {
        log.debug("Mapping User entity to UserResponse object for user ID '{}'", user.getId());
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        return response;
    }

    public UserResponse getUserById(Long id) {
        log.info("Attempting to fetch user by ID: {}", id);
        try {
            UserResponse userResponse = securityRepository.findUserDetailsBySecurityId(id)
                    .orElseThrow(() -> new UserNotFoundException("User with ID '" + id + "' not found"));

            log.info("Successfully fetched user: {}", userResponse.getUsername());
            return userResponse;
        } catch (UserNotFoundException e) {
            log.error("User with ID '{}' not found. Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching user with ID '{}'. Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again later.");
        }
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Attempting to update user with Security ID '{}'", id);

        Security security = securityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Security entry with ID '{}' not found for update", id);
                    return new UserNotFoundException("User with Security ID " + id + " not found");
                });

        Long userId = security.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Associated User with ID '{}' not found for update", userId);
                    return new UserNotFoundException("Associated User with ID " + userId + " not found");
                });

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdated(LocalDateTime.now());
        user.setRole("USER");
        userRepository.save(user);

        security.setLogin(request.getUsername());
        security.setPassword(passwordEncoder.encode(request.getPassword()));
        security.setUpdated(LocalDateTime.now());
        securityRepository.save(security);

        log.info("User with Security ID '{}' and User ID '{}' updated successfully", id, userId);

        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User Updated",
                "User with username: " + user.getUsername() + " has been successfully updated."
        ));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse partiallyUpdateUser(Long id, PartialUserRequest request) {
        log.info("Attempting to partially update user with Security ID '{}'", id);
        Security security = securityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Security entry with ID '{}' not found for partial update", id);
                    return new UserNotFoundException("User with Security ID " + id + " not found");
                });
        Long userId = security.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Associated User with ID '{}' not found for partial update", userId);
                    return new UserNotFoundException("Associated User with ID " + userId + " not found");
                });

        if (request.getUsername() != null && !request.getUsername().equals(security.getLogin())) {
            log.info("Updating username for Security ID '{}' and User ID '{}'", id, userId);
            security.setLogin(request.getUsername());
            user.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            log.info("Updating password for Security ID '{}'", id);
            security.setPassword(passwordEncoder.encode(request.getPassword()));
            security.setUpdated(LocalDateTime.now());
        }
        if (request.getEmail() != null) {
            log.info("Updating email for User ID '{}'", userId);
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            log.info("Updating firstName for User ID '{}'", userId);
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            log.info("Updating lastName for User ID '{}'", userId);
            user.setLastName(request.getLastName());
        }

        securityRepository.save(security);
        userRepository.save(user);
        log.info("User with Security ID '{}' and User ID '{}' partially updated successfully", id, userId);

        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User Updated",
                "User with username: " + user.getUsername() + " has been successfully partially updated."
        ));
        return mapToResponse(user);
    }
}