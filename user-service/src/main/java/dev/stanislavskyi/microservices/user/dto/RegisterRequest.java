package dev.stanislavskyi.microservices.user.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
}
