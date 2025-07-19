package dev.stanislavskyi.microservices.user.service;

import dev.stanislavskyi.microservices.user.dto.RegisterRequest;
import dev.stanislavskyi.microservices.user.dto.UserResponse;
import dev.stanislavskyi.microservices.user.model.User;
import dev.stanislavskyi.microservices.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    public UserResponse register(RegisterRequest request) {

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());

        return userResponse;
    }
}
