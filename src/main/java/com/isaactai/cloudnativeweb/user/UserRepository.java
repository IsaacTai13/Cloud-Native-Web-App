package com.isaactai.cloudnativeweb.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author tisaac
 */
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
}
