package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KhaltiRequestDto {

    private String public_key;
    @NotBlank(message = "Mobile number is required")
    private String mobile;
    @NotBlank(message = "Transaction Pin is required")
    private String transaction_pin;
    private Integer amount;
    @NotNull(message = "Product Identity is required")
    private UUID product_identity;
    private String product_name;

    private String paymentReference;
}
