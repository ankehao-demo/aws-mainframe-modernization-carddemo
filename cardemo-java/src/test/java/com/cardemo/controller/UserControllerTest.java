package com.cardemo.controller;

import com.cardemo.config.JwtAuthenticationFilter;
import com.cardemo.config.JwtTokenProvider;
import com.cardemo.config.SecurityConfig;
import com.cardemo.dto.UserListResponse;
import com.cardemo.enums.UserType;
import com.cardemo.exception.CardDemoException;
import com.cardemo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_asAdmin_returns200() throws Exception {
        UserListResponse response = new UserListResponse(
                List.of(new UserListResponse.UserItem("USER0001", "Regular", "User", UserType.USER)),
                0, 10, 1, 1);
        when(userService.listUsers(any())).thenReturn(response);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].userId").value("USER0001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_asRegularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_asAdmin_returns201() throws Exception {
        UserListResponse.UserItem item = new UserListResponse.UserItem(
                "NEWUSER1", "New", "User", UserType.USER);
        when(userService.createUser(any())).thenReturn(item);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"NEWUSER1\",\"password\":\"PASSWORD\"," +
                                "\"firstName\":\"New\",\"lastName\":\"User\",\"userType\":\"U\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSER1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_duplicateId_returns400() throws Exception {
        when(userService.createUser(any())).thenThrow(
                new CardDemoException("User ID already exists: USER0001"));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"USER0001\",\"password\":\"PASSWORD\"," +
                                "\"firstName\":\"Dup\",\"lastName\":\"User\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User ID already exists: USER0001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_asAdmin_returns200() throws Exception {
        UserListResponse.UserItem item = new UserListResponse.UserItem(
                "USER0001", "Updated", "User", UserType.USER);
        when(userService.updateUser(any(), any())).thenReturn(item);

        mockMvc.perform(put("/api/users/USER0001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_returns204() throws Exception {
        doNothing().when(userService).deleteUser("USER0001");

        mockMvc.perform(delete("/api/users/USER0001")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound_returns400() throws Exception {
        doThrow(new CardDemoException("User not found: INVALID"))
                .when(userService).deleteUser("INVALID");

        mockMvc.perform(delete("/api/users/INVALID")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found: INVALID"));
    }
}
