package com.fyp.health_sync.dtos;


import com.fyp.health_sync.enums.RatingType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDto {

    @NotNull(message = "ratings cannot be null")
    private double ratings;
    @Size(max = 255, message = "comment cannot be more than 255 characters")
    private String comment;
    @NotNull(message = "rating type cannot be null")
    @EnumValidator(enumClass = RatingType.class, message = "Rating type must be one of the following: DOCTOR, USER")
    private String ratingType;

}
