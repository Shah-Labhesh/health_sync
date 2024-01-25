package com.fyp.health_sync.dtos;


import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {

    @NotBlank(message = "Name is cannot be empty")
    @Pattern(regexp = "^[a-zA-Z ]{3,}$", message = "Name must not contain special characters and must be at least 3 characters long")
    private String name;

    @NotBlank(message = "Email is cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role is cannot be empty")
    @EnumValidator(enumClass = UserRole.class, message = "Role must be either DOCTOR or USER")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @NotBlank(message = "Password is cannot be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character")
    private String password;

}
