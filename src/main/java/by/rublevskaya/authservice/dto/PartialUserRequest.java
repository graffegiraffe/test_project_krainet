package by.rublevskaya.authservice.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class PartialUserRequest {
    private String username;
    private String password;

    @Email
    private String email;

    private String firstName;
    private String lastName;
}