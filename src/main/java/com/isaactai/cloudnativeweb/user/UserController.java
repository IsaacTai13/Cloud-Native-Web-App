package com.isaactai.cloudnativeweb.user;

import com.isaactai.cloudnativeweb.metrics.ApiObserved;
import com.isaactai.cloudnativeweb.metrics.ApiResourceTag;
import com.isaactai.cloudnativeweb.logging.AccessNote;
import com.isaactai.cloudnativeweb.user.dto.UserCreateRequest;
import com.isaactai.cloudnativeweb.user.dto.UserResponse;
import com.isaactai.cloudnativeweb.user.dto.UserUpdateRequest;
import com.isaactai.cloudnativeweb.user.verification.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tisaac
 */
@RestController
@RequestMapping("/v1/user")
@ApiResourceTag(resource = "User")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping()
    @AccessNote(
            label = "User",
            success = "User created successfully",
            clientWarn = "User create failed",
            serverError = "Unexpected error occurred during user creation"
    )
    @ApiObserved
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
    @ApiObserved
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
    @ApiObserved
    public ResponseEntity<UserResponse> getUser(
            @PathVariable int userId,
            Authentication auth) {

        UserResponse me = userService.getSelf(userId, auth.getName());
        return ResponseEntity.ok(me);
    }

    @GetMapping("/validateEmail")
    public ResponseEntity<Map<String, String>> validateEmail(
            @RequestParam("email") String mail,
            @RequestParam("token") String token
    ){
        emailVerificationService.verify(mail, token, Instant.now());
        return ResponseEntity.ok(Map.of("message", "Email verified"));
    }
}
