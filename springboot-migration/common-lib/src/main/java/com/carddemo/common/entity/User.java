package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 * Represents the USRSEC VSAM KSDS file.
 * SEC-USR-TYPE maps to Spring Security roles: 'A' -> ROLE_ADMIN, 'U' -> ROLE_USER.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** SEC-USR-ID PIC X(08) */
    @Id
    @Column(name = "user_id", nullable = false, length = 8)
    private String userId;

    /** SEC-USR-PWD PIC X(08) - stored as bcrypt hash in modernized system */
    @Column(name = "password", nullable = false)
    private String password;

    /** SEC-USR-FNAME PIC X(20) */
    @Column(name = "first_name", length = 20)
    private String firstName;

    /** SEC-USR-LNAME PIC X(20) */
    @Column(name = "last_name", length = 20)
    private String lastName;

    /** SEC-USR-TYPE PIC X(01) - 'A' for admin, 'U' for regular user */
    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;
}
