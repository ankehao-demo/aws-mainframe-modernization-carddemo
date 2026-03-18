package com.aws.carddemo.service;

import com.aws.carddemo.dto.UserDto;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    private UserSecurity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserSecurity.builder()
                .usrId("USER0001")
                .usrFname("JOHN")
                .usrLname("DOE")
                .usrPwd("PASSWORD")
                .usrType("U")
                .build();
    }

    @Test
    void listUsers_returnsList() {
        when(userSecurityRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDto> users = userAdminService.listUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsrId()).isEqualTo("USER0001");
    }

    @Test
    void addUser_success() {
        when(userSecurityRepository.existsById("NEWUSER1")).thenReturn(false);
        when(userSecurityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDto dto = UserDto.builder()
                .usrId("NEWUSER1")
                .usrFname("NEW")
                .usrLname("USER")
                .usrPwd("password")
                .usrType("u")
                .build();

        UserDto result = userAdminService.addUser(dto);

        assertThat(result.getUsrId()).isEqualTo("NEWUSER1");
        verify(userSecurityRepository).save(any());
    }

    @Test
    void addUser_duplicate_throwsException() {
        when(userSecurityRepository.existsById("USER0001")).thenReturn(true);

        UserDto dto = UserDto.builder()
                .usrId("USER0001")
                .usrFname("JOHN")
                .usrLname("DOE")
                .usrPwd("PASSWORD")
                .usrType("U")
                .build();

        assertThatThrownBy(() -> userAdminService.addUser(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User already exists");
    }

    @Test
    void updateUser_password() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDto dto = UserDto.builder().usrPwd("NEWPASS1").build();

        UserDto result = userAdminService.updateUser("USER0001", dto);
        assertThat(result).isNotNull();
    }

    @Test
    void updateUser_type() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserDto dto = UserDto.builder().usrType("a").build();

        UserDto result = userAdminService.updateUser("USER0001", dto);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteUser_success() {
        when(userSecurityRepository.existsById("USER0001")).thenReturn(true);

        userAdminService.deleteUser("USER0001");

        verify(userSecurityRepository).deleteById("USER0001");
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userSecurityRepository.existsById("UNKNOWN1")).thenReturn(false);

        assertThatThrownBy(() -> userAdminService.deleteUser("UNKNOWN1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
