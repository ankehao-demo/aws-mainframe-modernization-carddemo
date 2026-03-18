package com.aws.carddemo.security;

import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CardDemoUserDetailsService implements UserDetailsService {

    private final UserSecurityRepository userSecurityRepository;

    public CardDemoUserDetailsService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserSecurity user = userSecurityRepository.findById(username.toUpperCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String role = "A".equals(user.getUsrType().trim()) ? "ADMIN" : "USER";

        return new User(
                user.getUsrId().trim(),
                user.getUsrPwd().trim(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
