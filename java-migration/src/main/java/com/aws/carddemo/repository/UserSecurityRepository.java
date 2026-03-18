package com.aws.carddemo.repository;

import com.aws.carddemo.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {
}
