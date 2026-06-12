import { useEffect, useMemo, useState } from "react";
import { addToCart, getCart } from "../services/cart";
import ProductCard from "../components/ProductCard";
import getProducts from "../services/products";

const PRODUCTS_PER_PAGE = 20;

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("default");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [addedProductId, setAddedProductId] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);

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

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, sortBy, selectedCategory]);

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
      filtered.sort((a, b) => (Number(b.averageRating) || 0) - (Number(a.averageRating) || 0));
    }

    return filtered;
  }, [products, searchTerm, sortBy, selectedCategory]);

  const totalPages = Math.ceil(filteredProducts.length / PRODUCTS_PER_PAGE);

  const paginatedProducts = useMemo(() => {
    const startIndex = (currentPage - 1) * PRODUCTS_PER_PAGE;
    const endIndex = startIndex + PRODUCTS_PER_PAGE;

    return filteredProducts.slice(startIndex, endIndex);
  }, [filteredProducts, currentPage]);

  const handleAddToCart = (product) => {
    if (!product.inStock) return;

    addToCart(product);
    setAddedProductId(product.id);

    setTimeout(() => {
      setAddedProductId(null);
    }, 1200);
  };

  const goToPreviousPage = () => {
    setCurrentPage((page) => Math.max(page - 1, 1));
  };

  const goToNextPage = () => {
    setCurrentPage((page) => Math.min(page + 1, totalPages));
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

      {!loading && !error && filteredProducts.length > 0 && (
        <p style={styles.resultCount}>
          Showing {(currentPage - 1) * PRODUCTS_PER_PAGE + 1}-
          {Math.min(currentPage * PRODUCTS_PER_PAGE, filteredProducts.length)} of{" "}
          {filteredProducts.length} books
        </p>
      )}

      {loading ? (
        <p style={styles.noResult}>Loading books...</p>
      ) : error ? (
        <p style={styles.noResult}>{error}</p>
      ) : (
        <>
          <div style={styles.grid}>
            {paginatedProducts.length > 0 ? (
              paginatedProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  onAddToCart={handleAddToCart}
                  isAdded={addedProductId === product.id}
                />
              ))
            ) : (
              <p style={styles.noResult}>No books found.</p>
            )}
          </div>

          {totalPages > 1 && (
            <div style={styles.pagination}>
              <button
                type="button"
                onClick={goToPreviousPage}
                disabled={currentPage === 1}
                style={{
                  ...styles.paginationButton,
                  ...(currentPage === 1 ? styles.paginationButtonDisabled : {}),
                }}
              >
                Previous
              </button>

              <div style={styles.pageNumbers}>
                {Array.from({ length: totalPages }, (_, index) => {
                  const pageNumber = index + 1;

                  return (
                    <button
                      key={pageNumber}
                      type="button"
                      onClick={() => setCurrentPage(pageNumber)}
                      style={{
                        ...styles.pageNumberButton,
                        ...(currentPage === pageNumber
                          ? styles.activePageNumberButton
                          : {}),
                      }}
                    >
                      {pageNumber}
                    </button>
                  );
                })}
              </div>

              <button
                type="button"
                onClick={goToNextPage}
                disabled={currentPage === totalPages}
                style={{
                  ...styles.paginationButton,
                  ...(currentPage === totalPages ? styles.paginationButtonDisabled : {}),
                }}
              >
                Next
              </button>
            </div>
          )}
        </>
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
    marginBottom: "16px",
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
  resultCount: {
    textAlign: "center",
    color: "#6b5b53",
    fontSize: "15px",
    marginBottom: "28px",
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
  pagination: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    gap: "12px",
    flexWrap: "wrap",
    marginTop: "32px",
  },
  paginationButton: {
    padding: "10px 16px",
    borderRadius: "10px",
    border: "1px solid #6b4f3b",
    backgroundColor: "#6b4f3b",
    color: "#fff",
    fontSize: "15px",
    cursor: "pointer",
  },
  paginationButtonDisabled: {
    backgroundColor: "#c7b8ab",
    borderColor: "#c7b8ab",
    cursor: "not-allowed",
  },
  pageNumbers: {
    display: "flex",
    gap: "8px",
    flexWrap: "wrap",
    justifyContent: "center",
  },
  pageNumberButton: {
    width: "38px",
    height: "38px",
    borderRadius: "10px",
    border: "1px solid #6b4f3b",
    backgroundColor: "#fff",
    color: "#6b4f3b",
    fontWeight: "bold",
    cursor: "pointer",
  },
  activePageNumberButton: {
    backgroundColor: "#6b4f3b",
    color: "#fff",
  },
};

export default ProductList;