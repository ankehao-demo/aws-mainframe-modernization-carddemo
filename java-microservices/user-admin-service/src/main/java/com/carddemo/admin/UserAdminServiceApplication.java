package com.carddemo.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.carddemo.admin", "com.carddemo.common"})
public class UserAdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAdminServiceApplication.class, args);
    }
}
