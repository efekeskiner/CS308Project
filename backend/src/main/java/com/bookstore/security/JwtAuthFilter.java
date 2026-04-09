package com.bookstore.security;

import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
  public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
        private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtils jwtUtils, UserRepository userRepository) {
              this.jwtUtils = jwtUtils;
              this.userRepository = userRepository;
    }

    @Override
        protected void doFilterInternal(HttpServletRequest request,
                                                                            HttpServletResponse response,
                                                                            FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                          String token = authHeader.substring(7);

                              if (jwtUtils.validateToken(token)) {
                                                String email = jwtUtils.getEmailFromToken(token);
                                                String role  = jwtUtils.getRoleFromToken(token);

                              User user = userRepository.findByEmail(email).orElse(null);
                                                if (user != null) {
                                                                      UsernamePasswordAuthenticationToken auth =
                                                                                                new UsernamePasswordAuthenticationToken(
                                                                                                                              user,
                                                                                                                              null,
                                                                                                                              Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                                                                                                          );
                                                                      SecurityContextHolder.getContext().setAuthentication(auth);
                                                }
                              }
            }

            filterChain.doFilter(request, response);
                }
  }
