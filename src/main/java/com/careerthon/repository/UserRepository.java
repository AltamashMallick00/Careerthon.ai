package com.careerthon.repository;

import com.careerthon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier) OR u.phoneNumber = :cleanPhone OR u.phoneNumber = :rawPhone OR u.phoneNumber = :prefixedPhone")
    Optional<User> findFlexibleUser(
            @Param("identifier") String identifier,
            @Param("cleanPhone") String cleanPhone,
            @Param("rawPhone") String rawPhone,
            @Param("prefixedPhone") String prefixedPhone
    );
}
