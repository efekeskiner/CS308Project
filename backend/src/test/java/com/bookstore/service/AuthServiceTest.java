package com.bookstore.service;

import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
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

    @Mock
        private UserRepository userRepository;

    @InjectMocks
        private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private User buildUser(long id, String email, String rawPassword, Role role) {
              User user = new User();
              ReflectionTestUtils.setField(user, "id", id);
              user.setEmail(email);
              user.setPassword(encoder.encode(rawPassword));
              user.setRole(role);
              return user;
    }

    @Test
        void register_savesNewUser_whenEmailNotTaken() {
                  when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
                  when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = authService.register("new@test.com", "secret123", "Alice");

            assertNotNull(result);
                  assertEquals("new@test.com", result.getEmail());
                  verify(userRepository).save(any(User.class));
        }

    @Test
        void register_throwsException_whenEmailAlreadyTaken() {
                  User existing = buildUser(1L, "taken@test.com", "pass", Role.CUSTOMER);
                  when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(existing));

            assertThrows(RuntimeException.class,
                                         () -> authService.register("taken@test.com", "pass", "Bob"));
        }

    @Test
        void login_returnsUser_whenCredentialsAreValid() {
                  User user = buildUser(1L, "user@test.com", "correct", Role.CUSTOMER);
                  when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            User result = authService.login("user@test.com", "correct");

            assertNotNull(result);
                  assertEquals("user@test.com", result.getEmail());
        }

    @Test
        void login_throwsException_whenPasswordIsWrong() {
                  User user = buildUser(1L, "user@test.com", "correct", Role.CUSTOMER);
                  when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            assertThrows(RuntimeException.class,
                                         () -> authService.login("user@test.com", "wrongpassword"));
        }

    @Test
        void login_throwsException_whenEmailNotFound() {
                  when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                                         () -> authService.login("ghost@test.com", "any"));
        }

    @Test
        void getUserById_returnsUser_whenExists() {
                  User user = buildUser(5L, "five@test.com", "pw", Role.CUSTOMER);
                  when(userRepository.findById(5L)).thenReturn(Optional.of(user));

            User result = authService.getUserById(5L);

            assertNotNull(result);
                  assertEquals("five@test.com", result.getEmail());
        }

    @Test
        void getUserById_throwsException_whenNotFound() {
                  when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> authService.getUserById(99L));
        }
  }
