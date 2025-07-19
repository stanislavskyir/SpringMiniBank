package dev.stanislavskyi.microservices.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
}
