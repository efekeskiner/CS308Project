package com.bookstore.service;

import com.bookstore.dto.UpdateProfileRequest;
import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.dto.RefreshRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.dto.RegisterResponse;
import com.bookstore.dto.TokenRefreshResponse;
import com.bookstore.dto.UserDto;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
    public class AuthService {

    private final UserRepository userRepository;
            private final BCryptPasswordEncoder passwordEncoder;
            private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, JwtUtils jwtUtils) {
                this.userRepository = userRepository;
                this.passwordEncoder = new BCryptPasswordEncoder();
                this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                                    .orElseThrow(InvalidCredentialsException::new);

                if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                                throw new InvalidCredentialsException();
                }

                String accessToken  = jwtUtils.generateAccessToken(
                                    user.getId(), user.getEmail(), user.getRole().name());
                String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

                return new LoginResponse(accessToken, refreshToken, new UserDto(user));
    }

    public RegisterResponse register(RegisterRequest request) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                                throw new EmailAlreadyExistsException();
                }

                String hash = passwordEncoder.encode(request.getPassword());
                User user = new User();
                user.setName(request.getName());
                user.setEmail(request.getEmail());
                user.setPasswordHash(hash);
                user.setTaxId(request.getTaxId());
                user.setHomeAddress(request.getHomeAddress());
                user.setRole(Role.CUSTOMER);
                userRepository.save(user);

                return new RegisterResponse("Registration successful", new UserDto(user));
    }

    public TokenRefreshResponse refresh(String refreshToken) {
                if (!jwtUtils.validateToken(refreshToken)) {
                                throw new TokenRefreshException();
                }
                String email = jwtUtils.getEmailFromToken(refreshToken);
                User user = userRepository.findByEmail(email)
                                        .orElseThrow(TokenRefreshException::new);
                String newAccessToken  = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
                String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail());
                return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    public UserDto updateProfile(User currentUser, UpdateProfileRequest request) {
                User user = userRepository.findById(currentUser.getId())
                                        .orElseThrow(InvalidCredentialsException::new);

                user.setName(request.getName());
                user.setHomeAddress(request.getHomeAddress());

                User savedUser = userRepository.save(user);
                return new UserDto(savedUser);
        }

    public static class InvalidCredentialsException extends RuntimeException {
                public InvalidCredentialsException() {
                                super("Invalid credentials");
                }
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
                public EmailAlreadyExistsException() {
                                super("Email already in use");
                }
    }

    public static class TokenRefreshException extends RuntimeException {
                public TokenRefreshException() {
                                super("Refresh token is invalid or expired");
                }
    }
    }
