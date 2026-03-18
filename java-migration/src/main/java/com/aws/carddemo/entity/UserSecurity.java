package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_security")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecurity {

    @Id
    @Column(name = "usr_id", length = 8)
    private String usrId;

    @Column(name = "usr_fname", length = 20)
    private String usrFname;

    @Column(name = "usr_lname", length = 20)
    private String usrLname;

    @Column(name = "usr_pwd", length = 8, nullable = false)
    private String usrPwd;

    @Column(name = "usr_type", length = 1, nullable = false)
    private String usrType;
}
