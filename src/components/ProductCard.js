import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { isInWishlist, toggleWishlist } from "../services/wishlist";
import { isLoggedIn, authFetch, getCurrentUser } from "../services/auth";

const WISHLIST_API = "http://localhost:8080/api/wishlist";

const FALLBACK_IMAGE =
  "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='420' viewBox='0 0 300 420'%3E%3Crect width='300' height='420' fill='%23f3ece3'/%3E%3Crect x='45' y='55' width='210' height='310' rx='16' fill='%23ffffff' stroke='%23d1c7bc' stroke-width='3'/%3E%3Ctext x='150' y='195' text-anchor='middle' font-family='Arial' font-size='24' fill='%236b4f3b'%3ENo Image%3C/text%3E%3Ctext x='150' y='230' text-anchor='middle' font-family='Arial' font-size='16' fill='%238b7b72'%3EBook Cover%3C/text%3E%3C/svg%3E";

function ProductCard({ product, onAddToCart, isAdded }) {
  const [wishlisted, setWishlisted] = useState(false);
  const navigate = useNavigate();

  const currentUser = getCurrentUser();
  const isCustomer = !currentUser || currentUser.role === "CUSTOMER";

  useEffect(() => {
    let cancelled = false;

    const loadWishlistStatus = async () => {
      if (!product) return;

      if (!isCustomer) {
        setWishlisted(false);
        return;
      }

      if (!isLoggedIn()) {
        setWishlisted(isInWishlist(product.id));
        return;
      }

      try {
        const response = await authFetch(WISHLIST_API);

        if (!response.ok) {
          throw new Error("Wishlist could not be loaded");
        }

        const data = await response.json();

        const wishlistItems = Array.isArray(data)
          ? data
          : data?.content || data?.data || data?.items || [];

        const exists = wishlistItems.some((item) => {
          const wishlistProductId =
            item.product?.id ??
            item.product?.productId ??
            item.productId ??
            item.book?.id ??
            item.bookId ??
            item.id;

          return Number(wishlistProductId) === Number(product.id);
        });

        if (!cancelled) {
          setWishlisted(exists);
        }
      } catch (err) {
        console.error("Wishlist status could not be loaded:", err);
        if (!cancelled) {
          setWishlisted(false);
        }
      }
    };

    loadWishlistStatus();

    return () => {
      cancelled = true;
    };
  }, [product?.id, isCustomer]);

  const handleCardClick = () => {
    navigate(`/products/${product.id}`);
  };

  const handleWishlistToggle = async (event) => {
    event.stopPropagation();

    if (!isLoggedIn()) {
      const updatedWishlist = toggleWishlist(product);
      const nowWishlisted = updatedWishlist.some(
        (item) => Number(item.id) === Number(product.id)
      );
      setWishlisted(nowWishlisted);
      return;
    }

    const previousWishlisted = wishlisted;
    const nextWishlisted = !wishlisted;

    setWishlisted(nextWishlisted);

    try {
      if (nextWishlisted) {
        await authFetch(WISHLIST_API, {
          method: "POST",
          body: JSON.stringify({ productId: Number(product.id) }),
        });
      } else {
        await authFetch(`${WISHLIST_API}/${product.id}`, {
          method: "DELETE",
        });
      }
    } catch (err) {
      console.error("Wishlist could not be updated:", err);
      setWishlisted(previousWishlisted);
    }
  };

  const handleAddToCart = (event) => {
    event.stopPropagation();
    onAddToCart(product);
  };

  if (!product) return null;

  const hasDiscount = Number(product.discountRate) > 0;
  const imageSrc = product.imageUrl || FALLBACK_IMAGE;

  return (
    <div style={styles.card} onClick={handleCardClick}>
      <img
        src={imageSrc}
        alt={product.name || "Book"}
        style={styles.image}
        onError={(event) => {
          event.currentTarget.src = FALLBACK_IMAGE;
        }}
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

      {isCustomer && (
        <>
          <button
            style={{
              ...styles.button,
              ...(isAdded ? styles.addedButton : {}),
              ...(product.inStock ? {} : styles.disabledButton),
            }}
            disabled={!product.inStock}
            onClick={handleAddToCart}
          >
            {!product.inStock ? "Out of Stock" : isAdded ? "Added ✓" : "Add to Cart"}
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
        </>
      )}
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
  addedButton: {
    backgroundColor: "#15803d",
    transform: "scale(1.03)",
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