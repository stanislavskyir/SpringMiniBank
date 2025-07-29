package dev.stanislavskyi.microservices.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Schema(example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Schema(example = "Doe")
    private String lastName;

    @NotBlank(message = "KeycloakId is required")
    private String keycloakId;
}

