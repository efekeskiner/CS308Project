package com.bookstore.config;

import com.bookstore.model.*;
import com.bookstore.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReviewRepository reviewRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository,
                      OrderRepository orderRepository,
                      DeliveryRepository deliveryRepository,
                      InvoiceRepository invoiceRepository,
                      ReviewRepository reviewRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.deliveryRepository = deliveryRepository;
        this.invoiceRepository = invoiceRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        seedCategoriesAndProducts();
        seedManagers();
        seedCustomersAndOrders();
    }

    // ── Products ─────────────────────────────────────────────────────────────

    private void seedCategoriesAndProducts() {
        if (productRepository.count() > 0) return;

        Map<String, Category> cats = Map.of(
                "Fiction",    saveCat("Fiction"),
                "Science",    saveCat("Science"),
                "History",    saveCat("History"),
                "Philosophy", saveCat("Philosophy"),
                "Biography",  saveCat("Biography")
        );

        List<Product> products = new ArrayList<>();

        // ---- Fiction (10) ----
        products.add(book("The Great Gatsby", "1st Ed.", "9780743273565",
                "A story of decadence and excess in the Jazz Age.",
                12, "12.99", "No warranty", "Scribner", cats.get("Fiction"),
                "/images/books/great-gatsby.jpg"));
        products.add(book("1984", "Reissue", "9780451524935",
                "Dystopian novel about totalitarianism and surveillance.",
                25, "10.50", "No warranty", "Signet Classic", cats.get("Fiction"),
                "/images/books/1984.jpg"));
        products.add(book("To Kill a Mockingbird", "50th Anniv.", "9780061120084",
                "A child's view of racial injustice in the American South.",
                8, "11.20", "No warranty", "Harper Perennial", cats.get("Fiction"),
                "/images/books/mockingbird.jpg"));
        products.add(book("Dune", "1st Ed.", "9780441172719",
                "Epic science-fiction saga set on the desert planet Arrakis.",
                14, "15.00", "No warranty", "Ace Books", cats.get("Fiction"),
                "/images/books/dune.jpg"));
        products.add(book("Pride and Prejudice", "Penguin Ed.", "9780141439518",
                "Jane Austen's classic novel of manners, marriage, and misjudgement.",
                18, "9.75", "No warranty", "Penguin Classics", cats.get("Fiction"),
                null));
        products.add(book("The Catcher in the Rye", "Reissue", "9780316769488",
                "Holden Caulfield's wandering days in postwar New York.",
                10, "11.99", "No warranty", "Little, Brown", cats.get("Fiction"),
                null));
        products.add(book("Brave New World", "Reprint", "9780060850524",
                "Aldous Huxley's vision of a hedonistic technological dystopia.",
                7, "12.40", "No warranty", "Harper Perennial", cats.get("Fiction"),
                null));
        products.add(book("The Lord of the Rings", "Box Set", "9780544003415",
                "Tolkien's epic Middle-earth trilogy in one collection.",
                3, "39.99", "1-year publisher warranty", "Houghton Mifflin", cats.get("Fiction"),
                null));
        products.add(book("Crime and Punishment", "Penguin Ed.", "9780140449136",
                "Dostoevsky's psychological exploration of guilt and redemption.",
                6, "13.50", "No warranty", "Penguin Classics", cats.get("Fiction"),
                null));
        products.add(book("One Hundred Years of Solitude", "Reprint", "9780060883287",
                "García Márquez's multigenerational saga of the Buendía family.",
                9, "14.20", "No warranty", "Harper Perennial", cats.get("Fiction"),
                null));

        // ---- Science (8) ----
        products.add(book("A Brief History of Time", "Updated", "9780553380163",
                "Hawking's exploration of cosmology for the general reader.",
                10, "14.75", "No warranty", "Bantam", cats.get("Science"),
                "/images/books/brief-history-of-time.jpg"));
        products.add(book("The Selfish Gene", "40th Anniv.", "9780198788607",
                "Dawkins' landmark book on gene-centered evolution.",
                6, "13.40", "No warranty", "Oxford University Press", cats.get("Science"),
                "/images/books/selfish-gene.jpg"));
        products.add(book("Cosmos", "Reissue", "9780345539434",
                "Carl Sagan's classic tour of the universe.",
                9, "16.80", "No warranty", "Ballantine Books", cats.get("Science"),
                "/images/books/cosmos.jpg"));
        products.add(book("The Origin of Species", "Penguin Ed.", "9780451529060",
                "Darwin's foundational work on evolution by natural selection.",
                5, "10.99", "No warranty", "Signet Classic", cats.get("Science"),
                null));
        products.add(book("The Elegant Universe", "Reprint", "9780393338102",
                "Brian Greene on superstrings and the search for a unified theory.",
                4, "15.50", "No warranty", "W. W. Norton", cats.get("Science"),
                null));
        products.add(book("The Demon-Haunted World", "Reissue", "9780345409461",
                "Sagan's defense of scientific skepticism.",
                7, "13.10", "No warranty", "Ballantine Books", cats.get("Science"),
                null));
        products.add(book("Silent Spring", "Anniversary Ed.", "9780618249060",
                "Rachel Carson's exposé of pesticide dangers.",
                2, "12.85", "No warranty", "Mariner Books", cats.get("Science"),
                null));
        products.add(book("The Gene", "Reprint", "9781476733524",
                "Mukherjee's intimate history of genetics and human inheritance.",
                11, "17.25", "No warranty", "Scribner", cats.get("Science"),
                null));

        // ---- History (8) ----
        products.add(book("Sapiens", "1st Ed.", "9780062316097",
                "A brief history of humankind.",
                20, "18.00", "No warranty", "Harper", cats.get("History"),
                "/images/books/sapiens.jpg"));
        products.add(book("Guns, Germs, and Steel", "Reprint", "9780393354324",
                "Jared Diamond on the fates of human societies.",
                7, "17.25", "No warranty", "W. W. Norton", cats.get("History"),
                "/images/books/guns-germs-steel.jpg"));
        products.add(book("The Silk Roads", "Paperback", "9781101912379",
                "A new history of the world through the lens of trade.",
                5, "19.90", "No warranty", "Knopf", cats.get("History"),
                "/images/books/silk-roads.jpg"));
        products.add(book("A People's History of the United States", "Reprint", "9780062397348",
                "Howard Zinn's bottom-up retelling of American history.",
                8, "18.50", "No warranty", "Harper Perennial", cats.get("History"),
                null));
        products.add(book("Postwar", "Reprint", "9780143037750",
                "Tony Judt's history of Europe since 1945.",
                3, "22.00", "No warranty", "Penguin Books", cats.get("History"),
                null));
        products.add(book("SPQR", "Reprint", "9781631492228",
                "Mary Beard's history of ancient Rome.",
                6, "16.90", "No warranty", "Liveright", cats.get("History"),
                null));
        products.add(book("The Crusades", "Reissue", "9780060787295",
                "Thomas Asbridge on the war for the Holy Land.",
                4, "20.00", "No warranty", "Ecco", cats.get("History"),
                null));
        products.add(book("The Rise and Fall of the Third Reich", "Reissue", "9781451651683",
                "William L. Shirer's monumental account of Nazi Germany.",
                2, "24.50", "No warranty", "Simon & Schuster", cats.get("History"),
                null));

        // ---- Philosophy (8) ----
        products.add(book("Meditations", "Modern Library Ed.", "9780812968255",
                "Personal writings of Marcus Aurelius.",
                15, "9.99", "No warranty", "Modern Library", cats.get("Philosophy"),
                "/images/books/meditations.jpg"));
        products.add(book("Beyond Good and Evil", "Penguin Ed.", "9780140449235",
                "Nietzsche's critique of past philosophers.",
                11, "11.75", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                "/images/books/beyond-good-and-evil.jpg"));
        products.add(book("The Republic", "Penguin Ed.", "9780140455113",
                "Plato's dialogue on justice and the ideal state.",
                13, "10.90", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                "/images/books/republic.jpg"));
        products.add(book("Thus Spoke Zarathustra", "Penguin Ed.", "9780140441185",
                "Nietzsche's philosophical novel of the Übermensch.",
                7, "12.20", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                null));
        products.add(book("Critique of Pure Reason", "Cambridge Ed.", "9780521657297",
                "Kant's foundational work on the limits of human knowledge.",
                3, "21.50", "No warranty", "Cambridge University Press", cats.get("Philosophy"),
                null));
        products.add(book("The Prince", "Penguin Ed.", "9780140449150",
                "Machiavelli's pragmatic treatise on political power.",
                9, "8.99", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                null));
        products.add(book("The Symposium", "Penguin Ed.", "9780140449273",
                "Plato's dialogue on love and beauty.",
                5, "9.50", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                null));
        products.add(book("Discourse on Method", "Penguin Ed.", "9780140442069",
                "Descartes' essay on rational inquiry — 'I think, therefore I am.'",
                4, "8.75", "No warranty", "Penguin Classics", cats.get("Philosophy"),
                null));

        // ---- Biography (6) ----
        products.add(book("Steve Jobs", "Reprint", "9781451648539",
                "Walter Isaacson's biography of the Apple co-founder.",
                0, "20.00", "No warranty", "Simon & Schuster", cats.get("Biography"),
                "/images/books/steve-jobs.jpg"));
        products.add(book("Long Walk to Freedom", "Reissue", "9780316548182",
                "Autobiography of Nelson Mandela.",
                4, "16.50", "No warranty", "Back Bay Books", cats.get("Biography"),
                "/images/books/long-walk-to-freedom.jpg"));
        products.add(book("The Diary of a Young Girl", "Definitive Ed.", "9780553296983",
                "Anne Frank's diary written in hiding during the Nazi occupation.",
                12, "9.99", "No warranty", "Bantam", cats.get("Biography"),
                null));
        products.add(book("Benjamin Franklin: An American Life", "Reprint", "9780743258074",
                "Walter Isaacson on the most multifaceted of the founding fathers.",
                5, "18.00", "No warranty", "Simon & Schuster", cats.get("Biography"),
                null));
        products.add(book("Einstein: His Life and Universe", "Reprint", "9780743264747",
                "Walter Isaacson's biography of the iconic physicist.",
                6, "19.50", "No warranty", "Simon & Schuster", cats.get("Biography"),
                null));
        products.add(book("Alexander Hamilton", "Reprint", "9780143034759",
                "Ron Chernow's definitive biography of the founding father.",
                3, "22.00", "No warranty", "Penguin Books", cats.get("Biography"),
                null));

        productRepository.saveAll(products);
    }

    // ── Managers ─────────────────────────────────────────────────────────────

    private void seedManagers() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String pmEmail = "pm@bookstore.local";
        if (userRepository.findByEmail(pmEmail).isEmpty()) {
            User pm = new User();
            pm.setName("Product Manager");
            pm.setEmail(pmEmail);
            pm.setPasswordHash(encoder.encode("pm12345"));
            pm.setRole(Role.PRODUCT_MANAGER);
            userRepository.save(pm);
        }

        String smEmail = "sm@bookstore.local";
        if (userRepository.findByEmail(smEmail).isEmpty()) {
            User sm = new User();
            sm.setName("Sales Manager");
            sm.setEmail(smEmail);
            sm.setPasswordHash(encoder.encode("sm12345"));
            sm.setRole(Role.SALES_MANAGER);
            userRepository.save(sm);
        }
    }

    // ── Customers, Orders, Reviews ────────────────────────────────────────────

    private void seedCustomersAndOrders() {
        if (orderRepository.count() > 0) return;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Map<String, Product> p = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getName, x -> x, (a, b) -> a));

        // ── Customers ────────────────────────────────────────────────────────
        User alice = customer(encoder, "Alice Yılmaz",  "alice@demo.local",  "Atatürk Cad. No:12, Kadıköy, İstanbul");
        User burak = customer(encoder, "Burak Demir",   "burak@demo.local",  "Bağcılar Mah. No:5, Ankara");
        User ceren = customer(encoder, "Ceren Kaya",    "ceren@demo.local",  "Konak Sok. No:8, İzmir");
        User deniz = customer(encoder, "Deniz Arslan",  "deniz@demo.local",  "Çankaya Cad. No:21, Ankara");
        User elif  = customer(encoder, "Elif Şahin",    "elif@demo.local",   "Bornova Mah. No:3, İzmir");
        User fatih = customer(encoder, "Fatih Öztürk",  "fatih@demo.local",  "Beşiktaş No:7, İstanbul");
        User gizem = customer(encoder, "Gizem Çelik",   "gizem@demo.local",  "Karşıyaka Mah. No:15, İzmir");
        User hakan = customer(encoder, "Hakan Aydın",   "hakan@demo.local",  "Mecidiyeköy No:9, İstanbul");

        // ── Alice ─────────────────────────────────────────────────────────────
        // DELIVERED: 1984 + Meditations
        Order a1 = placeOrder(alice, alice.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("1984"), 1),
                new Item(p.get("Meditations"), 1));
        createDeliveries(a1, alice, true);
        createInvoice(a1);

        // PROCESSING: Dune
        Order a2 = placeOrder(alice, alice.getHomeAddress(), OrderStatus.PROCESSING,
                new Item(p.get("Dune"), 1));
        createDeliveries(a2, alice, false);

        // ── Burak ─────────────────────────────────────────────────────────────
        // DELIVERED: Sapiens x2 + The Great Gatsby
        Order b1 = placeOrder(burak, burak.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("Sapiens"), 2),
                new Item(p.get("The Great Gatsby"), 1));
        createDeliveries(b1, burak, true);
        createInvoice(b1);

        // IN_TRANSIT: Cosmos
        Order b2 = placeOrder(burak, burak.getHomeAddress(), OrderStatus.IN_TRANSIT,
                new Item(p.get("Cosmos"), 1));
        createDeliveries(b2, burak, false);

        // ── Ceren ─────────────────────────────────────────────────────────────
        // DELIVERED: Pride and Prejudice + The Republic
        Order c1 = placeOrder(ceren, ceren.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("Pride and Prejudice"), 1),
                new Item(p.get("The Republic"), 1));
        createDeliveries(c1, ceren, true);
        createInvoice(c1);

        // ── Deniz ─────────────────────────────────────────────────────────────
        // DELIVERED: A Brief History of Time
        Order d1 = placeOrder(deniz, deniz.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("A Brief History of Time"), 1));
        createDeliveries(d1, deniz, true);
        createInvoice(d1);

        // PROCESSING: Beyond Good and Evil
        Order d2 = placeOrder(deniz, deniz.getHomeAddress(), OrderStatus.PROCESSING,
                new Item(p.get("Beyond Good and Evil"), 1));
        createDeliveries(d2, deniz, false);

        // ── Elif ──────────────────────────────────────────────────────────────
        // DELIVERED: Guns, Germs, and Steel + Brave New World
        Order e1 = placeOrder(elif, elif.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("Guns, Germs, and Steel"), 1),
                new Item(p.get("Brave New World"), 1));
        createDeliveries(e1, elif, true);
        createInvoice(e1);

        // ── Fatih ─────────────────────────────────────────────────────────────
        // DELIVERED: To Kill a Mockingbird + One Hundred Years of Solitude
        Order f1 = placeOrder(fatih, fatih.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("To Kill a Mockingbird"), 1),
                new Item(p.get("One Hundred Years of Solitude"), 1));
        createDeliveries(f1, fatih, true);
        createInvoice(f1);

        // IN_TRANSIT: The Catcher in the Rye
        Order f2 = placeOrder(fatih, fatih.getHomeAddress(), OrderStatus.IN_TRANSIT,
                new Item(p.get("The Catcher in the Rye"), 1));
        createDeliveries(f2, fatih, false);

        // ── Gizem ─────────────────────────────────────────────────────────────
        // DELIVERED: The Selfish Gene
        Order g1 = placeOrder(gizem, gizem.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("The Selfish Gene"), 1));
        createDeliveries(g1, gizem, true);
        createInvoice(g1);

        // PROCESSING: The Silk Roads
        Order g2 = placeOrder(gizem, gizem.getHomeAddress(), OrderStatus.PROCESSING,
                new Item(p.get("The Silk Roads"), 1));
        createDeliveries(g2, gizem, false);

        // ── Hakan ─────────────────────────────────────────────────────────────
        // DELIVERED: Crime and Punishment + The Prince
        Order h1 = placeOrder(hakan, hakan.getHomeAddress(), OrderStatus.DELIVERED,
                new Item(p.get("Crime and Punishment"), 1),
                new Item(p.get("The Prince"), 1));
        createDeliveries(h1, hakan, true);
        createInvoice(h1);

        // PROCESSING: Alexander Hamilton
        Order h2 = placeOrder(hakan, hakan.getHomeAddress(), OrderStatus.PROCESSING,
                new Item(p.get("Alexander Hamilton"), 1));
        createDeliveries(h2, hakan, false);

        // ── Reviews ──────────────────────────────────────────────────────────
        // Alice reviewed from order a1 (DELIVERED)
        review(alice, p.get("1984"),        8, "Chilling and relevant — Orwell's vision cuts deep.", true);
        review(alice, p.get("Meditations"), 9, null, false); // rating only

        // Burak reviewed from order b1 (DELIVERED)
        review(burak, p.get("Sapiens"),           10, "Changed the way I see humanity's story.", true);
        review(burak, p.get("The Great Gatsby"),   7, "Beautifully written, but hard to root for anyone.", false); // pending

        // Ceren reviewed from order c1 (DELIVERED)
        review(ceren, p.get("Pride and Prejudice"), 9, "Austen's wit never gets old.", true);
        review(ceren, p.get("The Republic"),         6, null, false); // rating only

        // Deniz reviewed from order d1 (DELIVERED)
        review(deniz, p.get("A Brief History of Time"), 8, "Hawking makes the incomprehensible feel accessible.", false); // pending

        // Elif reviewed from order e1 (DELIVERED)
        review(elif, p.get("Guns, Germs, and Steel"), 9, "Every question I had about world history answered here.", true);
        review(elif, p.get("Brave New World"),         7, "Huxley's dystopia feels uncomfortably close to home.", true);

        // Fatih reviewed from order f1 (DELIVERED)
        review(fatih, p.get("To Kill a Mockingbird"),          10, "A timeless story of courage and justice.", true);
        review(fatih, p.get("One Hundred Years of Solitude"),   8, null, false); // rating only

        // Gizem reviewed from order g1 (DELIVERED)
        review(gizem, p.get("The Selfish Gene"), 7, "Dense but rewarding — Dawkins is a superb communicator.", false); // pending

        // Hakan reviewed from order h1 (DELIVERED)
        review(hakan, p.get("Crime and Punishment"), 9, "Raskolnikov's descent is one of literature's greatest portraits.", true);
        review(hakan, p.get("The Prince"),            5, null, false); // rating only
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static class Item {
        final Product product;
        final int quantity;
        Item(Product p, int q) { product = p; quantity = q; }
    }

    private User customer(BCryptPasswordEncoder enc, String name, String email, String address) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPasswordHash(enc.encode("demo1234"));
            u.setRole(Role.CUSTOMER);
            u.setHomeAddress(address);
            return userRepository.save(u);
        });
    }

    private Order placeOrder(User user, String address, OrderStatus status, Item... items) {
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(address);
        order.setStatus(status);

        BigDecimal total = BigDecimal.ZERO;
        for (Item spec : items) {
            BigDecimal unitPrice = spec.product.getPrice();
            OrderItem oi = new OrderItem();
            oi.setProduct(spec.product);
            oi.setQuantity(spec.quantity);
            oi.setUnitPrice(unitPrice);
            order.addItem(oi);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(spec.quantity)));
        }
        order.setTotalPrice(total);
        return orderRepository.save(order);
    }

    private void createDeliveries(Order order, User customer, boolean completed) {
        for (OrderItem item : order.getItems()) {
            Delivery d = new Delivery(customer, item.getProduct(), item.getQuantity(),
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                    order.getDeliveryAddress(), order);
            d.setIsCompleted(completed);
            deliveryRepository.save(d);
        }
    }

    private void createInvoice(Order order) {
        invoiceRepository.save(new Invoice(order, "./invoices/seed-placeholder.pdf"));
    }

    private void review(User user, Product product, int score, String content, boolean approved) {
        Review r = new Review(user, product, score);
        if (content != null) {
            r.setContent(content);
            r.setContentApproved(approved);
        }
        reviewRepository.save(r);
    }

    private Category saveCat(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private Product book(String name, String model, String isbn, String desc,
                         int stock, String price, String warranty,
                         String distributor, Category category,
                         String imageUrl) {
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
        p.setImageUrl(imageUrl);

        return p;
    }
}
