package com.buyio.auth.service;

import com.buyio.auth.dto.AuthResponse;
import com.buyio.auth.dto.LoginRequest;
import com.buyio.auth.dto.RegisterRequest;
import com.buyio.auth.dto.UserDto;
import com.buyio.auth.entity.Role;
import com.buyio.auth.entity.User;
import com.buyio.auth.entity.UserStatus;
import com.buyio.auth.exception.ResourceNotFoundException;
import com.buyio.auth.exception.UserAlreadyExistsException;
import com.buyio.auth.repository.RoleRepository;
import com.buyio.auth.repository.UserRepository;
import com.buyio.auth.security.JwtTokenProvider;
import com.buyio.auth.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserEventProducer eventProducer;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           UserEventProducer eventProducer) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.eventProducer = eventProducer;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        // Emitir evento de Auditoria vía Kafka
        eventProducer.sendUserEvent("USER_LOGGED_IN", userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario '" + registerRequest.getUsername() + "' ya está registrado.");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("El correo electrónico '" + registerRequest.getEmail() + "' ya está registrado.");
        }

        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Error: El rol ROLE_USER no existe en la base de datos."));
            roles.add(userRole);
        } else {
            registerRequest.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Error: El rol '" + roleName + "' no fue encontrado."));
                roles.add(role);
            });
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .createdBy("REGISTRATION_API")
                .updatedBy("REGISTRATION_API")
                .build();

        User savedUser = userRepository.save(user);

        // Emitir evento de Auditoria vía Kafka
        eventProducer.sendUserEvent("USER_REGISTERED", savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());

        return mapToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el username: " + username));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}