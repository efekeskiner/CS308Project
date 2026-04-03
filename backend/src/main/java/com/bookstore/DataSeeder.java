package com.bookstore;

import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        String hash = passwordEncoder.encode("password123");

        User customer = new User();
        customer.setName("Test Customer");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash(hash);
        customer.setRole(Role.CUSTOMER);

        User salesManager = new User();
        salesManager.setName("Test Sales Manager");
        salesManager.setEmail("sales@test.com");
        salesManager.setPasswordHash(hash);
        salesManager.setRole(Role.SALES_MANAGER);

        User productManager = new User();
        productManager.setName("Test Product Manager");
        productManager.setEmail("product@test.com");
        productManager.setPasswordHash(hash);
        productManager.setRole(Role.PRODUCT_MANAGER);

        userRepository.save(customer);
        userRepository.save(salesManager);
        userRepository.save(productManager);

        System.out.println("Seeded 3 test users (password: password123)");
    }
}
