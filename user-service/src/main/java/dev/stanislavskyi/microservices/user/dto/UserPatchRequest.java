package dev.stanislavskyi.microservices.user.dto;

import lombok.Data;

@Data
public class UserPatchRequest {
    private String firstName;
    private String lastName;
}


