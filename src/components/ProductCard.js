import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { isInWishlist, toggleWishlist } from "../services/wishlist";

function ProductCard({ product, onAddToCart }) {
  const [wishlisted, setWishlisted] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (product) {
      setWishlisted(isInWishlist(product.id));
    }
  }, [product]);

  const handleCardClick = () => {
    navigate(`/products/${product.id}`);
  };

  const handleWishlistToggle = (event) => {
    event.stopPropagation();

    const updatedWishlist = toggleWishlist(product);
    const exists = updatedWishlist.some((item) => item.id === product.id);
    setWishlisted(exists);
  };

  const handleAddToCart = (event) => {
    event.stopPropagation();
    onAddToCart(product);
  };

  if (!product) return null;

  const hasDiscount = Number(product.discountRate) > 0;

  return (
    <div style={styles.card} onClick={handleCardClick}>
      <img
        src={product.imageUrl || "https://via.placeholder.com/150?text=No+Image"}
        alt={product.name || "Book"}
        style={styles.image}
      />

      <h3 style={styles.name}>{product.name || "No Name"}</h3>

      <p style={styles.category}>{product.categoryName || "Uncategorized"}</p>

      <p style={styles.description}>
        {product.description || "No description available"}
      </p>

      <div style={styles.priceContainer}>
        {hasDiscount && (
          <span style={styles.originalPrice}>
            {Number(product.originalPrice).toFixed(2)} TL
          </span>
        )}

        <span style={styles.price}>
          {Number(product.price ?? 0).toFixed(2)} TL
        </span>

        {hasDiscount && (
          <span style={styles.discountRate}>
            %{Number(product.discountRate)} off
          </span>
        )}
      </div>

      <p style={styles.rating}>
        ★{" "}
        {product.averageRating !== null && product.averageRating !== undefined
          ? Number(product.averageRating).toFixed(1)
          : "N/A"}
        <span style={styles.ratingCount}> ({product.ratingCount ?? 0})</span>
      </p>

      <p style={styles.stock}>
        Stock: {product.inStock ? product.quantityInStock ?? 0 : "Out of stock"}
      </p>

      <button
        style={{
          ...styles.button,
          ...(product.inStock ? {} : styles.disabledButton),
        }}
        disabled={!product.inStock}
        onClick={handleAddToCart}
      >
        {product.inStock ? "Add to Cart" : "Out of Stock"}
      </button>

      <button
        style={{
          ...styles.wishlistButton,
          ...(wishlisted ? styles.wishlisted : {}),
        }}
        onClick={handleWishlistToggle}
      >
        {wishlisted ? "♥ Remove from Wishlist" : "♡ Add to Wishlist"}
      </button>
    </div>
  );
}

const styles = {
  card: {
    backgroundColor: "#ffffff",
    borderRadius: "16px",
    padding: "20px",
    boxShadow: "0 4px 14px rgba(0, 0, 0, 0.08)",
    textAlign: "center",
    transition: "transform 0.2s ease",
    cursor: "pointer",
  },
  image: {
    width: "100%",
    height: "260px",
    objectFit: "cover",
    borderRadius: "12px",
    marginBottom: "16px",
  },
  name: {
    fontSize: "24px",
    marginBottom: "8px",
    color: "#2f1e14",
  },
  category: {
    fontSize: "14px",
    color: "#8b5e3c",
    marginBottom: "8px",
    fontWeight: "bold",
  },
  description: {
    fontSize: "15px",
    color: "#444",
    marginBottom: "12px",
    minHeight: "60px",
  },
  priceContainer: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    gap: "8px",
    flexWrap: "wrap",
    marginBottom: "10px",
  },
  originalPrice: {
    textDecoration: "line-through",
    color: "#8b7b72",
    fontSize: "16px",
  },
  price: {
    fontSize: "24px",
    fontWeight: "bold",
    color: "#2f1e14",
  },
  discountRate: {
    fontSize: "13px",
    fontWeight: "bold",
    color: "#8b5e3c",
  },
  rating: {
    fontSize: "15px",
    fontWeight: "bold",
    color: "#4b2e2e",
    marginBottom: "8px",
  },
  ratingCount: {
    fontSize: "12px",
    color: "#777",
    fontWeight: "normal",
  },
  stock: {
    fontSize: "15px",
    fontWeight: "bold",
    color: "#2f1e14",
    marginBottom: "8px",
  },
  button: {
    marginTop: "12px",
    width: "100%",
    padding: "12px",
    border: "none",
    borderRadius: "10px",
    backgroundColor: "#6b4f3b",
    color: "white",
    fontSize: "16px",
    cursor: "pointer",
  },
  disabledButton: {
    backgroundColor: "#9ca3af",
    cursor: "not-allowed",
  },
  wishlistButton: {
    marginTop: "10px",
    width: "100%",
    padding: "12px",
    border: "1px solid #6b4f3b",
    borderRadius: "10px",
    backgroundColor: "#fff",
    color: "#6b4f3b",
    fontSize: "16px",
    cursor: "pointer",
  },
  wishlisted: {
    backgroundColor: "#fce7f3",
    color: "#9d174d",
    border: "1px solid #9d174d",
  },
};

export default ProductCard;