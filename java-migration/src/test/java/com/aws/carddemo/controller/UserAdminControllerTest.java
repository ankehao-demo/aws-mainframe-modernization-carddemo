package com.aws.carddemo.controller;

import com.aws.carddemo.config.SecurityConfig;
import com.aws.carddemo.dto.UserDto;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtAuthenticationFilter;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.UserAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAdminService userAdminService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_returns200() throws Exception {
        UserDto user = UserDto.builder()
                .usrId("USER0001")
                .usrFname("JOHN")
                .usrLname("DOE")
                .usrType("U")
                .build();

        when(userAdminService.listUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usrId").value("USER0001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUser_returns200() throws Exception {
        UserDto dto = UserDto.builder()
                .usrId("NEWUSER1")
                .usrFname("NEW")
                .usrLname("USER")
                .usrPwd("PASSWORD")
                .usrType("U")
                .build();

        when(userAdminService.addUser(any(UserDto.class))).thenReturn(dto);

        mockMvc.perform(post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usrId").value("NEWUSER1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_returns204() throws Exception {
        doNothing().when(userAdminService).deleteUser("USER0001");

        mockMvc.perform(delete("/api/admin/users/USER0001")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userAdminService).deleteUser("UNKNOWN1");

        mockMvc.perform(delete("/api/admin/users/UNKNOWN1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_regularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}
