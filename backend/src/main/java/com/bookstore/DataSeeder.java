package com.bookstore;

import com.bookstore.model.Product;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
    public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
            private final ProductRepository productRepository;
            private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository) {
                this.userRepository    = userRepository;
                this.productRepository = productRepository;
    }

    @Override
            public void run(String... args) {
                        if (userRepository.count() == 0) {
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

                if (productRepository.count() == 0) {
                                String[][] books = {
                                    {"The Great Gatsby",       "A story of wealth and obsession in the Jazz Age.",        "Classic",   "12.99", "50"},
                                    {"To Kill a Mockingbird",  "A powerful story of racial injustice and compassion.",    "Classic",   "10.99", "40"},
                                    {"1984",                   "A dystopian vision of a totalitarian surveillance state.", "Fiction",  "9.99",  "60"},
                                    {"Dune",                   "An epic tale of politics, religion and ecology in space.", "Sci-Fi",   "14.99", "35"},
                                    {"Clean Code",             "Principles and practices of writing clean software.",      "Technical","29.99", "25"},
                                    {"The Pragmatic Programmer","Timeless advice for software craftsmen.",                 "Technical","34.99", "20"},
                                    {"Brave New World",        "A chilling vision of a pleasure-driven future society.",  "Fiction",  "11.99", "45"},
                                    {"The Hobbit",             "A hobbit embarks on an unexpected adventure.",            "Fantasy",  "13.99", "55"}
                                };

                            for (String[] b : books) {
                                                Product p = new Product();
                                                p.setName(b[0]);
                                                p.setDescription(b[1]);
                                                p.setCategory(b[2]);
                                                p.setPrice(Double.parseDouble(b[3]));
                                                p.setStock(Integer.parseInt(b[4]));
                                                productRepository.save(p);
                            }
                                System.out.println("Seeded 8 sample products");
                }
            }
    }
