package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {

    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Name must be alphabetic")
    private String name;
    @Email(message = "Email must be valid")
    private String email;
    @Size(min = 8, message = "OldPassword must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", message = "OldPassword must contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String oldPassword;
    @Size(min = 8, message = "NewPassword must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$", message = "NewPassword must contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String newPassword;
}
