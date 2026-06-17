package com.example.ticketbooking.controller;

import com.example.ticketbooking.config.JwtTokenUtil;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST xu ly xac thuc va phan quyen nhu dang ky, dang nhap (Session
 * & Token).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Lay thong tin nguoi dung hien tai dang dang nhap.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Not authenticated\"}");
        }
        User user = (User) userService.loadUserByUsername(auth.getName());
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    /**
     * Dang ky tai khoan nguoi dung moi.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest regRequest) {
        if (userService.existsByUsername(regRequest.getUsername())) {
            return ResponseEntity.badRequest().body("{\"message\": \"Username is already taken\"}");
        }

        if (userService.existsByEmail(regRequest.getEmail())) {
            return ResponseEntity.badRequest().body("{\"message\": \"Email is already taken\"}");
        }

        try {
            User newUser = new User(
                    regRequest.getUsername(),
                    regRequest.getPassword(),
                    regRequest.getEmail(),
                    regRequest.getRole() != null ? regRequest.getRole() : Role.ROLE_BUYER);
            userService.registerUser(newUser);
            return ResponseEntity.ok("{\"message\": \"User registered successfully! Please sign in.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Registration failed: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Dang nhap theo co che Session-based (Cookies).
     */
    @PostMapping("/login/session")
    public ResponseEntity<?> loginSession(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tao session va luu Security Context de Spring Security xac nhap JSESSIONID o
            // cac request sau
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            User user = (User) authentication.getPrincipal();
            return ResponseEntity
                    .ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Invalid username or password\"}");
        }
    }

    /**
     * Dang nhap theo co che Token-based (JWT).
     */
    @PostMapping("/login/token")
    public ResponseEntity<?> loginToken(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            // Tao JWT Token gui lai cho Client
            String token = jwtTokenUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Invalid username or password\"}");
        }
    }

    // --- DTO Static Inner Classes ---

    public static class LoginRequest {
        @NotBlank(message = "Username cannot be blank")
        private String username;

        @NotBlank(message = "Password cannot be blank")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6)
        private String password;

        @NotBlank(message = "Email cannot be blank")
        @Email
        private String email;

        private Role role;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private Role role;

        public UserResponse(Long id, String username, String email, Role role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public Role getRole() {
            return role;
        }
    }
}
