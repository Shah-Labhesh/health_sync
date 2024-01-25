package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDoctorDto {

    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Name must be alphabetic")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Old Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Old Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String oldPassword;

    @Size(min = 8, message = "New Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "New Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String newPassword;

    @DecimalMin(value = "0.0", message = "Latitude must be greater than 0.0")
    private double latitude;

    @DecimalMin(value = "0.0", message = "Longitude must be greater than 0.0")
    private double longitude;

    @Size(min = 3, max = 50, message = "Address must be between 3 and 50 characters")
    private String address;

    private UUID speciality;
    private String experience;

    @Min(value = 100, message = "Fee must be greater than 0")
    private Integer fee;


    private MultipartFile image;

    @Pattern(regexp = "^(\\+)?(977)?([0-9]{10})$", message = "Invalid Khalti Id")
    private String khaltiId;
}
