package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.user.dto.UserCreateRequest;
import com.isaactai.cloudnativeweb.user.dto.UserResponse;
import com.isaactai.cloudnativeweb.user.dto.UserUpdateRequest;
import com.isaactai.cloudnativeweb.common.exception.BadRequestException;
import com.isaactai.cloudnativeweb.user.exception.DuplicateEmailException;
import com.isaactai.cloudnativeweb.common.exception.ForbiddenException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
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

    @Transactional
    public void updateSelf(int userId, String authUsername, UserUpdateRequest req) {
        // find curr login user
        User me = userRepo.findByUsername(authUsername)
                .orElseThrow(() -> new NotFoundException("user not found"));

        // confirm only can modify itself
        if (me.getId() != userId) {
            throw new ForbiddenException("Action Forbidden");
        }

        // at least one field need to be updated
        boolean changed = false;
        if (req.firstName() != null) {
            me.setFirstName(req.firstName());
            changed = true;
        }
        if (req.lastName() != null) {
            me.setLastName(req.lastName());
            changed = true;
        }
        if (req.password() != null) {
            me.setPwdHash(encoder.encode(req.password()));
            changed = true;
        }

        if (!changed) {
            throw new BadRequestException("No updatable fields provided");
        }

        // save it back to db
        userRepo.save(me);
    }

    public UserResponse getSelf(int userId, String authUsername) {
        User me = userRepo.findByUsername(authUsername)
                .orElseThrow(() -> new NotFoundException("auth user not found"));

        if (me.getId() != userId) {
            throw new ForbiddenException("Action Forbidden");
        }

        return new UserResponse(
                me.getId(),
                me.getFirstName(),
                me.getLastName(),
                me.getUsername(),
                me.getCreateTime(),
                me.getUpdatedTime()
        );
    }

    @Transactional
    public User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}