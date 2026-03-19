


package com.imran.aimonitoring.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.imran.aimonitoring.entity.User;
import com.imran.aimonitoring.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email));

        return new CustomUserDetails(user);
    }
}