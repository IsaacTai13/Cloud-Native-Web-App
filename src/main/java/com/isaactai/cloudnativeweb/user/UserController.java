package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.logging.AccessNote;
import com.isaactai.cloudnativeweb.user.dto.UserCreateRequest;
import com.isaactai.cloudnativeweb.user.dto.UserResponse;
import com.isaactai.cloudnativeweb.user.dto.UserUpdateRequest;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @author tisaac
 */
@RestController
@RequestMapping("/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @AccessNote(
            label = "User",
            success = "User created successfully",
            clientWarn = "User create failed",
            serverError = "Unexpected error occurred during user creation"
    )
    @Timed(value = "api.user.create", description = "Time taken to create a new user")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        UserResponse created = userService.createUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    @AccessNote(
            label = "User",
            success = "User updated successfully",
            clientWarn = "User update failed",
            serverError = "Unexpected error occurred during user update"
    )
    @Timed(value = "api.user.update", description = "Time taken to update a user")
    public ResponseEntity<Void> updateUser(
            @PathVariable int userId,
            @Valid @RequestBody UserUpdateRequest req,
            Authentication auth
    ) {
        userService.updateSelf(userId, auth.getName(), req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @AccessNote(
            label = "User",
            success = "User retrieved successfully",
            clientWarn = "User retrieval failed",
            serverError = "Unexpected error occurred during user retrieval"
    )
    @Timed(value = "api.user.get", description = "Time taken to retrieve a user")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable int userId,
            Authentication auth) {

        UserResponse me = userService.getSelf(userId, auth.getName());
        return ResponseEntity.ok(me);
    }
}
