package com.careerthon.service;

import com.careerthon.model.User;
import com.careerthon.repository.UserRepository;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Identifier cannot be empty");
        }
        String cleanIdentifier = identifier.trim();

        // Search by username, email, or phone number
        User user = userRepository.findByUsername(cleanIdentifier)
                .or(() -> userRepository.findByEmail(cleanIdentifier))
                .or(() -> userRepository.findByPhoneNumber(cleanIdentifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + cleanIdentifier));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(), // enabled
                true,             // accountNonExpired
                true,             // credentialsNonExpired
                true,             // accountNonLocked
                AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles())
        );
    }
}
