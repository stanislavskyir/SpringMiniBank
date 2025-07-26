package dev.stanislavskyi.microservices.user.service;

import dev.stanislavskyi.microservices.user.dto.RegisterRequest;
import dev.stanislavskyi.microservices.user.dto.UserPatchRequest;
import dev.stanislavskyi.microservices.user.dto.UserResponse;
import dev.stanislavskyi.microservices.user.dto.UserReplaceRequest;
import dev.stanislavskyi.microservices.user.exception.UserAlreadyExistsException;
import dev.stanislavskyi.microservices.user.exception.UserNotFoundException;
import dev.stanislavskyi.microservices.user.mapper.UserMapper;
import dev.stanislavskyi.microservices.user.model.User;
import dev.stanislavskyi.microservices.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public UserResponse createUser(RegisterRequest request) {
        userRepository.findByKeycloakId(request.getKeycloakId()).ifPresent(
                user -> {
                    throw new UserAlreadyExistsException("User with this keycloakId already exists");
                });

        User user = userMapper.fromRegisterRequest(request);
        User savedUser = userRepository.save(user);

        log.info("Created user: id={}, keycloakId={}", savedUser.getId(), savedUser.getKeycloakId());


        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

    }

    @Transactional
    public UserResponse replaceUser(UUID id, UserReplaceRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        log.info("User fully replaced: id={}, firstName={}, lastName={}", user.getId(), user.getFirstName(), user.getLastName());


        return userMapper.toResponse(user);

    }


    @Transactional
    public UserResponse patchUser(UUID id, UserPatchRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        log.info("User partially updated: id={}, firstName={}, lastName={}", user.getId(), user.getFirstName(), user.getLastName());


        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);

    }

}
