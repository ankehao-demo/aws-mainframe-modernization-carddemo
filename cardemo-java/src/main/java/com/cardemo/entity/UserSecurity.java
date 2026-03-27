package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User security entity derived from CSUSR01Y.cpy (SEC-USER-DATA, RECLN 80).
 *
 * COBOL field mappings:
 *   SEC-USR-ID      PIC X(08)  -> String (primary key)
 *   SEC-USR-FNAME   PIC X(20)  -> String
 *   SEC-USR-LNAME   PIC X(20)  -> String
 *   SEC-USR-PWD     PIC X(08)  -> String (stored as BCrypt hash in Java)
 *   SEC-USR-TYPE    PIC X(01)  -> String ('A' = admin, 'U' = user)
 */
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "usr_id", length = 8)
    private String userId;

    @Column(name = "usr_fname", length = 20)
    private String firstName;

    @Column(name = "usr_lname", length = 20)
    private String lastName;

    @Column(name = "usr_pwd", length = 72)
    private String password;

    @Column(name = "usr_type", length = 1)
    private String userType;

    public UserSecurity() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
