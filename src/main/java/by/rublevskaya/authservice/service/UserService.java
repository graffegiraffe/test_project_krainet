package by.rublevskaya.authservice.service;

import by.rublevskaya.authservice.dto.PartialUserRequest;
import by.rublevskaya.authservice.dto.UserRequest;
import by.rublevskaya.authservice.dto.UserResponse;
import by.rublevskaya.authservice.model.User;
import by.rublevskaya.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role("USER")
                .build();
        userRepository.save(user);
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found!"));
        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found!"));
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole("USER");
        userRepository.save(user);
        return mapToResponse(user);
    }

    public UserResponse partiallyUpdateUser(Long id, PartialUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found!"));
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getPassword() != null) user.setPassword(request.getPassword());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        userRepository.save(user);
        return mapToResponse(user);
    }
}