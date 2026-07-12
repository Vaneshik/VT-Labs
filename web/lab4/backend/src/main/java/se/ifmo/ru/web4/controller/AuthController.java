package se.ifmo.ru.web4.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import se.ifmo.ru.web4.model.User;
import se.ifmo.ru.web4.repository.UserRepository;
import se.ifmo.ru.web4.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public String username;
        public String role;

        public LoginResponse(String token, String username, String role) {
            this.token = token;
            this.username = username;
            this.role = role;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = new User();
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setEnabled(true);
        user.setRole("USER");

        String hashedPassword = passwordEncoder.encode(request.password);
        user.setPassword(hashedPassword);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.username,
                        request.password
                );

        try {
            Authentication auth = authenticationManager.authenticate(authToken);
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) auth.getPrincipal();

            String username = userDetails.getUsername();
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority()) // e.g. "ROLE_USER"
                    .orElse("ROLE_USER"); // или что-то по умолчанию

            // Генерируем JWT
            String token = jwtService.generateToken(username, role);

            // Возвращаем JSON с полями token, username и т.д.
            return ResponseEntity.ok(new LoginResponse(token, username, role));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }
}