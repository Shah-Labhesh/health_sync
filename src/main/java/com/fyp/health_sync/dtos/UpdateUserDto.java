package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


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
    @DecimalMin(value = "0.0", message = "Latitude must be greater than 0.0")
    private double latitude;
    @DecimalMin(value = "0.0", message = "Longitude must be greater than 0.0")
    private double longitude;
    private UUID speciality;
    private String experience;
    @Min(value = 100, message = "Fee must be greater than 100")
    private Integer fee;
    private MultipartFile profileImage;
    @Pattern(regexp = "^(\\+)?(977)?([0-9]{10})$", message = "Invalid Khalti Id")
    private String khaltiId;

    private Boolean textNotification;
}
