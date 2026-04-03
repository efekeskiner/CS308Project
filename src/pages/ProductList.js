import { useMemo, useState } from "react";
import ProductCard from "../components/ProductCard";
import mockProducts from "../data/mockProducts";

function ProductList() {
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("default");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const categories = ["All", ...new Set(mockProducts.map((p) => p.category))];

  const filteredProducts = useMemo(() => {
    let filtered = mockProducts.filter((product) => {
      const searchText =
        `${product.name} ${product.description} ${product.author}`.toLowerCase();

      const matchesSearch = searchText.includes(searchTerm.toLowerCase());
      const matchesCategory =
        selectedCategory === "All" || product.category === selectedCategory;

      return matchesSearch && matchesCategory;
    });

    if (sortBy === "priceAsc") {
      filtered.sort((a, b) => a.price - b.price);
    } else if (sortBy === "priceDesc") {
      filtered.sort((a, b) => b.price - a.price);
    } else if (sortBy === "popularity") {
      filtered.sort((a, b) => b.popularity - a.popularity);
    }

    return filtered;
  }, [searchTerm, sortBy, selectedCategory]);

  return (
    <div style={styles.page}>
      <h1 style={styles.title}>Book Store</h1>
      <p style={styles.subtitle}>
        Discover books by title, author, category, price, and popularity.
      </p>

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

      <div style={styles.grid}>
        {filteredProducts.length > 0 ? (
          filteredProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))
        ) : (
          <p style={styles.noResult}>No books found.</p>
        )}
      </div>
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