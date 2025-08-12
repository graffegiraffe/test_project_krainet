package by.rublevskaya.authservice.service;

import by.rublevskaya.authservice.clients.NotificationClient;
import by.rublevskaya.authservice.dto.NotificationRequest;
import by.rublevskaya.authservice.dto.PartialUserRequest;
import by.rublevskaya.authservice.dto.UserRequest;
import by.rublevskaya.authservice.dto.UserResponse;
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

    public String deleteUser(Long id) {
        log.info("Attempting to delete user with ID '{}'", id);
        if (!userRepository.existsById(id)) {
            log.error("User with ID '{}' not found for deletion", id);
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        Optional<User> user = userRepository.findById(id);
        securityRepository.deleteById(id);
        userRepository.deleteById(id);

        log.info("User with ID '{}' deleted successfully", id);
        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User deleted",
                "User " + user.get().getUsername() + " was removed from the system."
        ));
        return "User with ID " + id + " deleted successfully!";
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
        log.info("Fetching user with ID '{}'", id);
        User user = userRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("User with ID '{}' not found", id);
                    return new UserNotFoundException("User with ID " + id + " not found");
                });
        log.info("User with ID '{}' retrieved successfully", id);
        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Updating user with ID '{}'", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID '{}' not found for update", id);
                    return new UserNotFoundException("User with ID " + id + " not found");
                });
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdated(LocalDateTime.now());
        user.setRole("USER");

        Security security = securityRepository.findByLogin(user.getUsername());
        if (security != null) {
            security.setPassword(request.getPassword());
            security.setUpdated(LocalDateTime.now());
            securityRepository.save(security);
        }

        userRepository.save(user);
        log.info("User with ID '{}' updated successfully", id);

        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User Updated",
                "User with username: " + user.getUsername() + " has been successfully updated."
        ));
        return mapToResponse(user);
    }

    public UserResponse partiallyUpdateUser(Long id, PartialUserRequest request) {
        log.info("Partially updating user with ID '{}'", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID '{}' not found for partial update", id);
                    return new UserNotFoundException("User with ID " + id + " not found");
                });

        if (request.getUsername() != null) {
            log.info("Updating username for User ID '{}'", id);
            user.setUsername(request.getUsername());
            Security security = securityRepository.findByLogin(user.getUsername());
            if (security != null) {
                security.setLogin(request.getUsername());
                securityRepository.save(security);
            }
        }
        if (request.getPassword() != null) {
            log.info("Updating password for User ID '{}'", id);
            Security security = securityRepository.findByLogin(user.getUsername());
            if (security != null) {
                security.setPassword(request.getPassword());
                security.setUpdated(LocalDateTime.now());
                securityRepository.save(security);
                log.info("Password updated for User ID '{}'", id);
            } else {
                log.error("No security record found for User ID '{}'", id);
                throw new UserNotFoundException("Security record for User ID " + id + " not found");
            }
        }
        if (request.getEmail() != null) {
            log.info("Updating email for User ID '{}'", id);
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            log.info("Updating firstName for User ID '{}'", id);
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            log.info("Updating lastName for User ID '{}'", id);
            user.setLastName(request.getLastName());
        }

        userRepository.save(user);
        log.info("User with ID '{}' partially updated successfully", id);
        notificationClient.sendNotification(new NotificationRequest(
                adminEmail,
                "User Updated",
                "User with username: " + user.getUsername() + " has been successfully partially updated."
        ));
        return mapToResponse(user);
    }

    public Long getUserIdByUsername(String username) {
        Security user = securityRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user.getId();
    }
}