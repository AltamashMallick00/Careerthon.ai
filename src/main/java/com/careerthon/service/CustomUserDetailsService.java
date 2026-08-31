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
        String numericOnly = cleanIdentifier.replaceAll("[^0-9]", "");
        String cleanPhone = numericOnly.length() >= 10 ? numericOnly.substring(numericOnly.length() - 10) : numericOnly;
        String rawPhone = cleanIdentifier;
        String prefixedPhone = !cleanPhone.isEmpty() ? "+91" + cleanPhone : cleanIdentifier;

        // Search flexibly by username (case-insensitive), email (case-insensitive), or phone formats
        User user = userRepository.findFlexibleUser(cleanIdentifier, cleanPhone, rawPhone, prefixedPhone)
                .or(() -> userRepository.findByUsernameIgnoreCase(cleanIdentifier))
                .or(() -> userRepository.findByEmailIgnoreCase(cleanIdentifier))
                .or(() -> userRepository.findByPhoneNumber(cleanIdentifier))
                .or(() -> !cleanPhone.isEmpty() ? userRepository.findByPhoneNumber(cleanPhone) : java.util.Optional.empty())
                .or(() -> !cleanPhone.isEmpty() ? userRepository.findByPhoneNumber("+91" + cleanPhone) : java.util.Optional.empty())
                .orElseThrow(() -> {
                    System.err.println("❌ [AUTH FAILED] User not found for identifier: '" + cleanIdentifier + "' | Total Users in Database: " + userRepository.count());
                    return new UsernameNotFoundException("User not found with identifier: " + cleanIdentifier);
                });

        System.out.println("✅ [AUTH SUCCESS] User authenticated: " + user.getUsername() + " | Roles: " + user.getRoles());

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
