package com.conectea.backend.controller;

import com.conectea.backend.model.Role;
import com.conectea.backend.model.User;
import com.conectea.backend.repository.RoleRepository;
import com.conectea.backend.repository.UserRepository;
import com.conectea.backend.service.UserService;
import com.conectea.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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
        logger.info("Registrando novo usuário: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Tentativa de registro com email já cadastrado: {}", user.getEmail());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("message", "Email já cadastrado"));
        }

        try {
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER não encontrada"));
            user.setRoles(Set.of(userRole));
            
            if (user.getBirthDate() == null) {
                logger.warn("Data de nascimento não fornecida para o usuário: {}", user.getEmail());
            }
            
            logger.debug("Senha original antes da codificação para {}: {}", user.getEmail(), user.getPassword());
            
            // Criptografar senha
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            logger.debug("Senha codificada para {}: {}", user.getEmail(), user.getPassword());
            
            // Verificação imediata da senha
            boolean immediateMatch = passwordEncoder.matches(user.getPassword(), user.getPassword());
            logger.debug("Verificação imediata p/ {}: {}", user.getEmail(), immediateMatch);
            
            User newUser = userService.save(user);
            
            // Verificação pós-persistência
            User dbUser = userService.findUserByEmail(user.getEmail());
            if (dbUser != null) {
                boolean dbMatch = passwordEncoder.matches(user.getPassword(), dbUser.getPassword());
                logger.debug("Verificação pós-persistência p/ {}: {}", user.getEmail(), dbMatch);
            }
            
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            logger.error("Erro no registro: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", "Erro no cadastro: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        logger.info("Tentativa de login para: {}", email);
        logger.debug("Senha recebida no login: {}", password);
        
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            
            User user = userService.findUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Usuário não encontrado"));
            }

            logger.debug("Autenticação bem-sucedida para: {}", email);
            
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
            User user = userService.findUserByEmail(email);
            if (user != null) {
                boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
                logger.error("Falha na autenticação para {}: passwordMatch={}", email, passwordMatch);
                logger.error("Possíveis causas: Codificação diferente, espaços extras, ou senha incorreta");
            } else {
                logger.error("Usuário não encontrado: {}", email);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Credenciais inválidas"));
        }
    }
}