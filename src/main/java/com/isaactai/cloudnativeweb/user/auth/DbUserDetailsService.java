package com.isaactai.cloudnativeweb.user.auth;

import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Convert our User entity into a Spring Security UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPwdHash())
                .authorities("USER")
                .build();
    }
}
