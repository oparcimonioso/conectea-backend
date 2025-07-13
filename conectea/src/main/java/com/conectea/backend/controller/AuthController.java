package com.conectea.backend.controller;

import com.conectea.backend.model.Role;
import com.conectea.backend.model.User;
import com.conectea.backend.repository.RoleRepository;
import com.conectea.backend.repository.UserRepository;
import com.conectea.backend.service.UserService;
import com.conectea.backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(UserService userService,
                          RoleRepository roleRepository,
                          AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("message", "Email já cadastrado"));
        }

        try {
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
            user.setRoles(Set.of(userRole));
            
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User newUser = userService.save(user);
            
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", "Erro no cadastro: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            
            User user = userService.findUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Usuário não encontrado"));
            }

            List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

            String token = jwtUtil.generateToken(email, roles);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "email", email,
                "roles", roles,
                "name", user.getFirstName() + " " + user.getLastName()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Credenciais inválidas"));
        }
    }
}