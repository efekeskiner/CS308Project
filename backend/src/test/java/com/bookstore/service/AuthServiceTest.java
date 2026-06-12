package com.bookstore.service;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.dto.RegisterResponse;
import com.bookstore.dto.TokenRefreshResponse;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private User buildUser(long id, String email, String rawPassword, Role role) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("new@test.com");
        req.setPassword("secret123");
        req.setTaxId("12345678901");
        req.setHomeAddress("Somewhere 1");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        RegisterResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("new@test.com", res.getUser().getEmail());
        assertEquals(Role.CUSTOMER, res.getUser().getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throws_whenEmailAlreadyTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Bob");
        req.setEmail("taken@test.com");
        req.setPassword("secret123");

        when(userRepository.findByEmail("taken@test.com"))
                .thenReturn(Optional.of(buildUser(1L, "taken@test.com", "x", Role.CUSTOMER)));

        assertThrows(AuthService.EmailAlreadyExistsException.class,
                () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokens_whenCredentialsValid() {
        User user = buildUser(1L, "user@test.com", "correct", Role.CUSTOMER);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResponse res = authService.login(loginRequest("user@test.com", "correct"));

        assertEquals("access-token", res.getAccessToken());
        assertEquals("refresh-token", res.getRefreshToken());
        assertEquals("user@test.com", res.getUser().getEmail());
    }

    @Test
    void login_throws_whenPasswordWrong() {
        User user = buildUser(1L, "user@test.com", "correct", Role.CUSTOMER);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThrows(AuthService.InvalidCredentialsException.class,
                () -> authService.login(loginRequest("user@test.com", "wrongpassword")));
    }

    @Test
    void login_throws_whenEmailNotFound() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(AuthService.InvalidCredentialsException.class,
                () -> authService.login(loginRequest("ghost@test.com", "any")));
    }

    @Test
    void refresh_issuesNewTokens_whenRefreshTokenValid() {
        User user = buildUser(1L, "user@test.com", "pw", Role.CUSTOMER);
        when(jwtUtils.validateToken("good-refresh")).thenReturn(true);
        when(jwtUtils.getEmailFromToken("good-refresh")).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("new-access");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-refresh");

        TokenRefreshResponse res = authService.refresh("good-refresh");

        assertEquals("new-access", res.getAccessToken());
        assertEquals("new-refresh", res.getRefreshToken());
    }

    @Test
    void refresh_throws_whenRefreshTokenInvalid() {
        when(jwtUtils.validateToken("bad-token")).thenReturn(false);

        assertThrows(AuthService.TokenRefreshException.class,
                () -> authService.refresh("bad-token"));
    }
}
