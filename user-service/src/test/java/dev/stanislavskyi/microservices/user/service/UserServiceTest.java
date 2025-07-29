package dev.stanislavskyi.microservices.user.service;

import dev.stanislavskyi.microservices.user.dto.RegisterRequest;
import dev.stanislavskyi.microservices.user.dto.UserPatchRequest;
import dev.stanislavskyi.microservices.user.dto.UserReplaceRequest;
import dev.stanislavskyi.microservices.user.dto.UserResponse;
import dev.stanislavskyi.microservices.user.exception.UserAlreadyExistsException;
import dev.stanislavskyi.microservices.user.exception.UserNotFoundException;
import dev.stanislavskyi.microservices.user.mapper.UserMapper;
import dev.stanislavskyi.microservices.user.model.User;
import dev.stanislavskyi.microservices.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;
    private UserResponse userResponse;
    private RegisterRequest registerRequest;
    private UserReplaceRequest replaceRequest;
    private UserPatchRequest patchRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setKeycloakId("keycloak-123");

        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setKeycloakId("keycloak-123");

        replaceRequest = new UserReplaceRequest();
        replaceRequest.setFirstName("Jane");
        replaceRequest.setLastName("Smith");

        patchRequest = new UserPatchRequest();
        patchRequest.setFirstName("Jane");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.findByKeycloakId(registerRequest.getKeycloakId())).thenReturn(Optional.empty());
        when(userMapper.fromRegisterRequest(registerRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.createUser(registerRequest);

        // Assert
        assertThat(result).isEqualTo(userResponse);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenKeycloakIdExists() {
        // Arrange
        when(userRepository.findByKeycloakId(registerRequest.getKeycloakId())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with this keycloakId already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFindAllUsersWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        Page<UserResponse> result = userService.findAll(pageable);

        // Assert
        assertThat(result.getContent().get(0)).isEqualTo(userResponse);
        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponse(user);
    }


    @Test
    void shouldFindUserByIdSuccessfully() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.findById(userId);

        // Assert
        assertThat(result).isEqualTo(userResponse);
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void shouldReplaceUserSuccessfully() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.replaceUser(userId, replaceRequest);

        // Assert
        assertThat(result).isEqualTo(userResponse);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenReplacingNonExistentUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.replaceUser(userId, replaceRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void shouldPatchUserSuccessfully() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.patchUser(userId, patchRequest);

        // Assert
        assertThat(result).isEqualTo(userResponse);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe");
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenPatchingNonExistentUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUser(userId, patchRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository, never()).deleteById(userId);
    }

//    @Test
//    void createUser_successfully_creates_new_user() {
//        //Arrange
//        RegisterRequest request = new RegisterRequest();
//        request.setKeycloakId(KEYCLOAK_ID);
//        request.setFirstName("John");
//        request.setLastName("Smith");
//
//        User mappedUser = new User();
//        mappedUser.setKeycloakId(KEYCLOAK_ID);
//
//        User savedUser = new User();
//
//        UUID id = UUID.fromString(ID_STRING);
//        savedUser.setId(id);
//
//        savedUser.setKeycloakId(KEYCLOAK_ID);
//
//        UserResponse response = new UserResponse();
//        response.setId(id);
//        response.setKeycloakId(KEYCLOAK_ID);
//
//        when(mockRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());
//        when(mockMapper.fromRegisterRequest(request)).thenReturn(mappedUser);
//        when(mockRepository.save(mappedUser)).thenReturn(savedUser);
//        when(mockMapper.toResponse(savedUser)).thenReturn(response);
//
//        //Act
//        UserResponse result = userService.createUser(request);
//
//        //Assert
//        assertThat(result.getId()).isEqualTo(id);
//        assertThat(result.getKeycloakId()).isEqualTo(KEYCLOAK_ID);
//
//        verify(mockRepository).findByKeycloakId(KEYCLOAK_ID);
//        verify(mockRepository).save(mappedUser);
//        verify(mockMapper).fromRegisterRequest(request);
//        verify(mockMapper).toResponse(savedUser);
//    }
}