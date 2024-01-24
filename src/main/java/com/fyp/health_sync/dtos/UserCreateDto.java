package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    @NotBlank(message = "Name is cannot be empty")
    @Pattern(regexp = "^[a-zA-Z ]{4,}$", message = "Name must not contain special characters and must be at least 4 characters long")
    private String name;

    @NotBlank(message = "Email is cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is cannot be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character")
    private String password;

}
