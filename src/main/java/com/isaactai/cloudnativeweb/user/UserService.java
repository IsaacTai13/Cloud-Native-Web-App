package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.user.dto.UserCreateRequest;
import com.isaactai.cloudnativeweb.user.dto.UserResponse;
import com.isaactai.cloudnativeweb.user.exception.DuplicateEmailException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author tisaac
 */
@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest req) {
        // 400 if duplicate email
        if (userRepo.existsByUsername(req.username())) {
            throw new DuplicateEmailException();
        }

        User newUser = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .username(req.username())
                .pwdHash(encoder.encode(req.password()))
                .build();

        User saved = userRepo.save(newUser);

        return new UserResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getUsername(),
                saved.getCreateTime(),
                saved.getUpdatedTime()
        );
    }
}