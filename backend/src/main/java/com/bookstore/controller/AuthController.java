package com.bookstore.controller;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.dto.RegisterResponse;
import com.bookstore.dto.UserDto;
import com.bookstore.model.User;
import com.bookstore.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "http://localhost:3000")
    public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
                this.authService = authService;
    }

    @PostMapping("/login")
            public ResponseEntity<?> login(@RequestBody LoginRequest request) {
                        try {
                                        LoginResponse response = authService.login(request);
                                        return ResponseEntity.ok(response);
                        } catch (AuthService.InvalidCredentialsException e) {
                                        return ResponseEntity.status(401)
                                                                .body(Map.of("message", "Invalid credentials"));
                        }
            }

    @PostMapping("/register")
            public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
                        try {
                                        RegisterResponse response = authService.register(request);
                                        return ResponseEntity.status(201).body(response);
                        } catch (AuthService.EmailAlreadyExistsException e) {
                                        return ResponseEntity.status(409)
                                                                .body(Map.of("message", "Email already in use"));
                        }
            }

    /** Mevcut kullanıcı bilgisini döner — token doğrulama ve /me için */
    @GetMapping("/me")
            public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
                        if (user == null) {
                                        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
                        }
                        return ResponseEntity.ok(new UserDto(user));
            }
    }
