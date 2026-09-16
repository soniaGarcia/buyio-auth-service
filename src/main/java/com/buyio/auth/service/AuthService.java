package com.buyio.auth.service;

import com.buyio.auth.dto.AuthResponse;
import com.buyio.auth.dto.LoginRequest;
import com.buyio.auth.dto.RegisterRequest;
import com.buyio.auth.dto.UserDto;

import java.util.List;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    UserDto register(RegisterRequest registerRequest);

    UserDto getCurrentUser(String username);

    List<UserDto> getAllUsers();
}