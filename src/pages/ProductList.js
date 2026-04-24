import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { addToCart, getCart } from "../services/cart";
import ProductCard from "../components/ProductCard";
import getProducts from "../services/products";

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("default");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const navigate = useNavigate();
  const [cartCount, setCartCount] = useState(
    getCart().reduce((sum, item) => sum + item.quantity, 0)
  );

  useEffect(() => {
    getProducts()
      .then((data) => {
        setProducts(data.content || []);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError("Products could not be loaded.");
        setLoading(false);
      });
  }, []);

  const categories = [
    "All",
    ...new Set(products.map((p) => p.categoryName).filter(Boolean)),
  ];

  const filteredProducts = useMemo(() => {
    let filtered = products.filter((product) => {
      const searchText =
        `${product.name || ""} ${product.description || ""} ${product.author || ""}`.toLowerCase();

      const matchesSearch = searchText.includes(searchTerm.toLowerCase());
      const matchesCategory =
        selectedCategory === "All" || product.categoryName === selectedCategory;

      return matchesSearch && matchesCategory;
    });

    if (sortBy === "priceAsc") {
      filtered.sort((a, b) => (a.price || 0) - (b.price || 0));
    } else if (sortBy === "priceDesc") {
      filtered.sort((a, b) => (b.price || 0) - (a.price || 0));
    } else if (sortBy === "popularity") {
      filtered.sort((a, b) => {
        const scoreA =
          (Number(a.averageRating) || 0) * (Number(a.ratingCount) || 0);
        const scoreB =
          (Number(b.averageRating) || 0) * (Number(b.ratingCount) || 0);

        return scoreB - scoreA;
      });
    }

    return filtered;
  }, [products, searchTerm, sortBy, selectedCategory]);

  const handleAddToCart = (product) => {
    if (!product.inStock) return;

    const updatedCart = addToCart(product);
    const totalItems = updatedCart.reduce((sum, item) => sum + item.quantity, 0);
    setCartCount(totalItems);
  };

  return (
    <div style={styles.page}>
      <div style={styles.headerRow}>
        <div>
          <h1 style={styles.title}>Book Store</h1>
          <p style={styles.subtitle}>
            Discover books by title, author, category, price, and popularity.
          </p>
        </div>

        <button style={styles.cartButton} onClick={() => navigate("/cart")}>
          Cart ({cartCount})
        </button>
      </div>

      <div style={styles.topBar}>
        <input
          type="text"
          placeholder="Search by title, author or description..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={styles.searchInput}
        />

        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          style={styles.select}
        >
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>

        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          style={styles.select}
        >
          <option value="default">Sort By</option>
          <option value="priceAsc">Price: Low to High</option>
          <option value="priceDesc">Price: High to Low</option>
          <option value="popularity">Popularity</option>
        </select>
      </div>

      {loading ? (
        <p style={styles.noResult}>Loading books...</p>
      ) : error ? (
        <p style={styles.noResult}>{error}</p>
      ) : (
        <div style={styles.grid}>
          {filteredProducts.length > 0 ? (
            filteredProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={handleAddToCart}
              />
            ))
          ) : (
            <p style={styles.noResult}>No books found.</p>
          )}
        </div>
      )}
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    padding: "32px",
    background:
      "linear-gradient(180deg, #f8f4ee 0%, #f3ece3 50%, #efe5d8 100%)",
    fontFamily: "Arial, sans-serif",
  },
  headerRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "16px",
    flexWrap: "wrap",
  },
  cartButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "12px 16px",
    fontSize: "16px",
    cursor: "pointer",
  },
  title: {
    textAlign: "center",
    fontSize: "42px",
    marginBottom: "10px",
    color: "#4b2e2e",
  },
  subtitle: {
    textAlign: "center",
    fontSize: "18px",
    color: "#6b5b53",
    marginBottom: "28px",
  },
  topBar: {
    display: "flex",
    justifyContent: "center",
    gap: "16px",
    flexWrap: "wrap",
    marginBottom: "32px",
  },
  searchInput: {
    width: "360px",
    padding: "12px 14px",
    borderRadius: "10px",
    border: "1px solid #d1c7bc",
    fontSize: "16px",
    outline: "none",
  },
  select: {
    padding: "12px 14px",
    borderRadius: "10px",
    border: "1px solid #d1c7bc",
    fontSize: "16px",
    outline: "none",
    backgroundColor: "#fff",
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
    gap: "24px",
    alignItems: "stretch",
  },
  noResult: {
    textAlign: "center",
    fontSize: "18px",
    color: "#6b5b53",
    gridColumn: "1 / -1",
  },
};

export default ProductList;