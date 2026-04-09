import { useEffect, useState } from "react";
import { isInWishlist, toggleWishlist } from "../services/wishlist";


function ProductCard({ product, onAddToCart }) {
  const [wishlisted, setWishlisted] = useState(false);

  useEffect(() => {
    if (product) {
      setWishlisted(isInWishlist(product.id));
    }
  }, [product]);

  const handleWishlistToggle = () => {
    const updatedWishlist = toggleWishlist(product);
    const exists = updatedWishlist.some((item) => item.id === product.id);
    setWishlisted(exists);
  };
  if (!product) return null;

  return (
    <div style={styles.card}>
      <img
        src={product.image}
        alt={product.name || "Book"}
        style={styles.image}
      />

      <h3 style={styles.name}>{product.name || "No Name"}</h3>

      <p style={styles.author}>by {product.author || "Unknown Author"}</p>

      <p style={styles.category}>{product.category || "Uncategorized"}</p>

      <p style={styles.model}>{product.model || "-"}</p>

      <p style={styles.description}>
        {product.description || "No description available"}
      </p>

      <p>
        <strong>ISBN:</strong> {product.serialNumber || "-"}
      </p>

      <p>
        <strong>Distributor:</strong> {product.distributorInfo || "-"}
      </p>

      <p>
        <strong>Language:</strong> {product.language || "-"}
      </p>

      <p>
        <strong>Warranty:</strong> {product.warrantyStatus || "-"}
      </p>

      <p>
        <strong>Price:</strong> {product.price ?? 0} TL
      </p>

      <p>
        <strong>Rating:</strong> {product.averageRating ?? "N/A"}
      </p>

      <p>
        <strong>Stock:</strong>{" "}
        {product.inStock ? product.quantityInStock ?? 0 : "Out of stock"}
      </p>

      <button
        style={{
          ...styles.button,
          ...(product.inStock ? {} : styles.disabledButton),
        }}
        disabled={!product.inStock}
        onClick={() => onAddToCart(product)}
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
  author: {
    fontSize: "16px",
    color: "#6b5b53",
    marginBottom: "6px",
  },
  category: {
    fontSize: "14px",
    color: "#8b5e3c",
    marginBottom: "8px",
    fontWeight: "bold",
  },
  model: {
    fontSize: "15px",
    color: "#555",
    marginBottom: "10px",
  },
  description: {
    fontSize: "15px",
    color: "#444",
    marginBottom: "12px",
    minHeight: "60px",
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