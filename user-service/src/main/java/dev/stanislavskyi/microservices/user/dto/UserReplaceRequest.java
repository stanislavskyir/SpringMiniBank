package dev.stanislavskyi.microservices.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserReplaceRequest {
    @NotBlank
    @Size(max = 50)
    private String firstName;


    @NotBlank
    @Size(max = 50)
    private String lastName;
}
