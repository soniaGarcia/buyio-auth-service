package com.buyio.auth.controller;

import com.buyio.auth.dto.AuthResponse;
import com.buyio.auth.dto.LoginRequest;
import com.buyio.auth.dto.RegisterRequest;
import com.buyio.auth.dto.UserDto;
import com.buyio.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDto registeredUser = authService.register(registerRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}