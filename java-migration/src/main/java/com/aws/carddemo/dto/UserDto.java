package com.aws.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String usrId;
    private String usrFname;
    private String usrLname;
    private String usrPwd;
    private String usrType;
}
