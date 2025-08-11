package by.rublevskaya.authservice.service;

import by.rublevskaya.authservice.dto.PartialUserRequest;
import by.rublevskaya.authservice.dto.UserRequest;
import by.rublevskaya.authservice.dto.UserResponse;
import by.rublevskaya.authservice.exception.DataConflictException;
import by.rublevskaya.authservice.exception.UserNotFoundException;
import by.rublevskaya.authservice.model.User;
import by.rublevskaya.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
                .password(request.getPassword())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role("USER")
                .build();
        userRepository.save(user);

        log.info("User with ID '{}' created successfully", user.getId());
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

    public void deleteUser(Long id) {
        log.info("Attempting to delete user with ID '{}'", id);
        if (!userRepository.existsById(id)) {
            log.error("User with ID '{}' not found for deletion", id);
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
        log.info("User with ID '{}' deleted successfully", id);
    }

    private UserResponse mapToResponse(User user) {
        log.debug("Mapping User entity to UserResponse object for user ID '{}'", user.getId());
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
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
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole("USER");
        userRepository.save(user);
        log.info("User with ID '{}' updated successfully", id);
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
        }
        if (request.getPassword() != null) {
            log.info("Updating password for User ID '{}'", id);
            user.setPassword(request.getPassword());
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
        return mapToResponse(user);
    }
}