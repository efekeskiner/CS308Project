package com.bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
  @EnableWebSecurity
  public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
              this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                  http
                                .cors().and()
                                .csrf().disable()
                                .sessionManagement()
                                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and()
                                .authorizeRequests()
                                    // Herkese acik endpointler
                                    .antMatchers("/api/auth/**").permitAll()
                                    .antMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                                    // Swagger UI
                                    .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                    // Urun yonetimi - sadece PRODUCT_MANAGER
                                    .antMatchers(HttpMethod.POST, "/api/products/**").hasRole("PRODUCT_MANAGER")
                                    .antMatchers(HttpMethod.PUT, "/api/products/**").hasRole("PRODUCT_MANAGER")
                                    .antMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("PRODUCT_MANAGER")
                                    // Geriye kalan her istek icin login gerekli
                                    .anyRequest().authenticated()
                                .and()
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }
  }
