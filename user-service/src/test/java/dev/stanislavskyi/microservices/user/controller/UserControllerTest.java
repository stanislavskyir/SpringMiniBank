package dev.stanislavskyi.microservices.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.stanislavskyi.microservices.user.dto.RegisterRequest;
import dev.stanislavskyi.microservices.user.dto.UserPatchRequest;
import dev.stanislavskyi.microservices.user.dto.UserReplaceRequest;
import dev.stanislavskyi.microservices.user.dto.UserResponse;
import dev.stanislavskyi.microservices.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE = "/api/v1/users";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/users - create new user and return 201 with Location header")
    void shouldCreateUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setKeycloakId("abc-123");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.keycloakId").value("abc-123"));
    }

    @Test
    @DisplayName("GET /api/v1/users - get paginated list of users")
    void shouldGetAllUsersWithPagination() throws Exception {
        for (int i = 1; i <= 2; i++) {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("Name" + i);
            req.setLastName("Surname" + i);
            req.setKeycloakId("keyc" + i);
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get(BASE)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - get user by id")
    void shouldGetUserById() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Alice");
        req.setLastName("Wonder");
        req.setKeycloakId("kc-456");

        String json = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(json, UserResponse.class);
        UUID id = created.getId();

        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - replace existing user")
    void shouldReplaceUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Bob");
        req.setLastName("Builder");
        req.setKeycloakId("kc-789");

        String json = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(json, UserResponse.class);
        UUID id = created.getId();

        UserReplaceRequest replaceReq = new UserReplaceRequest();
        replaceReq.setFirstName("Robert");
        replaceReq.setLastName("BuilderNew");

        mockMvc.perform(put(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replaceReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("BuilderNew"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id} - patch existing user")
    void shouldPatchUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Tom");
        req.setLastName("Thumb");
        req.setKeycloakId("kc-101");

        String json = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(json, UserResponse.class);
        UUID id = created.getId();

        UserPatchRequest patchReq = new UserPatchRequest();
        patchReq.setLastName("ThumbNew");

        mockMvc.perform(patch(BASE + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("ThumbNew"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - delete user and return 204")
    void shouldDeleteUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Mark");
        req.setLastName("Twain");
        req.setKeycloakId("kc-202");

        String json = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(json, UserResponse.class);
        UUID id = created.getId();

        mockMvc.perform(delete(BASE + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isNotFound());
    }
}