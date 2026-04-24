package com.bookstore.config;

import com.bookstore.model.Category;
import com.bookstore.model.Product;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        seedCategoriesAndProducts();
        seedProductManager();
    }

    private void seedCategoriesAndProducts() {
        if (productRepository.count() > 0) return;

        Map<String, Category> cats = Map.of(
                "Fiction", saveCat("Fiction"),
                "Science", saveCat("Science"),
                "History", saveCat("History"),
                "Philosophy", saveCat("Philosophy"),
                "Biography", saveCat("Biography")
        );

        List<Product> products = List.of(
                book("The Great Gatsby", "1st Ed.", "9780743273565",
                        "A story of decadence and excess in the Jazz Age.",
                        12, "12.99", "No warranty", "Scribner", cats.get("Fiction")),
                book("1984", "Reissue",         "9780451524935",
                        "Dystopian novel about totalitarianism and surveillance.",
                        25, "10.50", "No warranty", "Signet Classic", cats.get("Fiction")),
                book("To Kill a Mockingbird", "50th Anniv.", "9780061120084",
                        "A child's view of racial injustice in the American South.",
                        8, "11.20", "No warranty", "Harper Perennial", cats.get("Fiction")),
                book("Dune", "1st Ed.",         "9780441172719",
                        "Epic science-fiction saga set on the desert planet Arrakis.",
                        14, "15.00", "No warranty", "Ace Books", cats.get("Fiction")),
                book("A Brief History of Time", "Updated",  "9780553380163",
                        "Hawking's exploration of cosmology for the general reader.",
                        10, "14.75", "No warranty", "Bantam", cats.get("Science")),
                book("The Selfish Gene", "40th Anniv.",     "9780198788607",
                        "Dawkins' landmark book on gene-centered evolution.",
                        6, "13.40", "No warranty", "Oxford University Press", cats.get("Science")),
                book("Cosmos", "Reissue",                   "9780345539434",
                        "Carl Sagan's classic tour of the universe.",
                        9, "16.80", "No warranty", "Ballantine Books", cats.get("Science")),
                book("Sapiens", "1st Ed.",                  "9780062316097",
                        "A brief history of humankind.",
                        20, "18.00", "No warranty", "Harper", cats.get("History")),
                book("Guns, Germs, and Steel", "Reprint",   "9780393354324",
                        "Jared Diamond on the fates of human societies.",
                        7, "17.25", "No warranty", "W. W. Norton", cats.get("History")),
                book("The Silk Roads", "Paperback",         "9781101912379",
                        "A new history of the world through the lens of trade.",
                        5, "19.90", "No warranty", "Knopf", cats.get("History")),
                book("Meditations", "Modern Library Ed.",   "9780812968255",
                        "Personal writings of Marcus Aurelius.",
                        15, "9.99", "No warranty", "Modern Library", cats.get("Philosophy")),
                book("Beyond Good and Evil", "Penguin Ed.", "9780140449235",
                        "Nietzsche's critique of past philosophers.",
                        11, "11.75", "No warranty", "Penguin Classics", cats.get("Philosophy")),
                book("The Republic", "Penguin Ed.",         "9780140455113",
                        "Plato's dialogue on justice and the ideal state.",
                        13, "10.90", "No warranty", "Penguin Classics", cats.get("Philosophy")),
                book("Steve Jobs", "Reprint",               "9781451648539",
                        "Walter Isaacson's biography of the Apple co-founder.",
                        0,  "20.00", "No warranty", "Simon & Schuster", cats.get("Biography")),
                book("Long Walk to Freedom", "Reissue",     "9780316548182",
                        "Autobiography of Nelson Mandela.",
                        4, "16.50", "No warranty", "Back Bay Books", cats.get("Biography"))
        );

        productRepository.saveAll(products);
    }

    private void seedProductManager() {
        String email = "pm@bookstore.local";
        if (userRepository.findByEmail(email).isPresent()) return;

        User pm = new User();
        pm.setName("Product Manager");
        pm.setEmail(email);
        pm.setPasswordHash(new BCryptPasswordEncoder().encode("pm12345"));
        pm.setRole(Role.PRODUCT_MANAGER);
        userRepository.save(pm);
    }

    private Category saveCat(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private Product book(String name, String model, String isbn, String desc,
                         int stock, String price, String warranty,
                         String distributor, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setModel(model);
        p.setSerialNumber(isbn);
        p.setDescription(desc);
        p.setQuantityInStock(stock);
        BigDecimal priceVal = new BigDecimal(price);
        p.setPrice(priceVal);
        p.setOriginalPrice(priceVal);
        p.setDiscountRate(BigDecimal.ZERO);
        p.setWarrantyStatus(warranty);
        p.setDistributorInfo(distributor);
        p.setCategory(category);
        return p;
    }
}
