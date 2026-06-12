import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authFetch, isLoggedIn } from "../services/auth";
import { getWishlist, removeFromWishlist } from "../services/wishlist";
import { addToCart } from "../services/cart";

const BASE_URL = "http://localhost:8080/api";

const FALLBACK_IMAGE =
  "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='420' viewBox='0 0 300 420'%3E%3Crect width='300' height='420' fill='%23f3ece3'/%3E%3Crect x='45' y='55' width='210' height='310' rx='16' fill='%23ffffff' stroke='%23d1c7bc' stroke-width='3'/%3E%3Ctext x='150' y='195' text-anchor='middle' font-family='Arial' font-size='24' fill='%236b4f3b'%3ENo Image%3C/text%3E%3Ctext x='150' y='230' text-anchor='middle' font-family='Arial' font-size='16' fill='%238b7b72'%3EBook Cover%3C/text%3E%3C/svg%3E";

function WishlistPage() {
  const [wishlist, setWishlist] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const loggedIn = isLoggedIn();

  useEffect(() => {
    if (loggedIn) {
      authFetch(`${BASE_URL}/wishlist`)
        .then((r) => r.json())
        .then((data) => setWishlist(Array.isArray(data) ? data : []))
        .catch(() => setWishlist(getWishlist()))
        .finally(() => setLoading(false));
    } else {
      setWishlist(getWishlist());
      setLoading(false);
    }
  }, [loggedIn]);

  const handleRemove = (productId) => {
    if (loggedIn) {
      authFetch(`${BASE_URL}/wishlist/${productId}`, { method: "DELETE" })
        .then(() => setWishlist((prev) => prev.filter((p) => (p.productId || p.id) !== productId)))
        .catch(() => alert("Could not remove from wishlist."));
    } else {
      const updated = removeFromWishlist(productId);
      setWishlist(updated);
    }
  };

  const handleAddToCart = (product) => {
    addToCart({
      id: product.productId || product.id,
      name: product.productName || product.name,
      price: product.price,
      imageUrl: product.imageUrl || product.image || "",
    });
    alert("Added to cart!");
  };

  if (loading) return <div style={styles.page}><p>Loading wishlist...</p></div>;

  return (
    <div style={styles.page}>
      <h1 style={styles.title}>My Wishlist</h1>

      {wishlist.length === 0 ? (
        <div style={styles.emptyBox}>
          <p style={styles.empty}>Your wishlist is empty.</p>
          <button style={styles.button} onClick={() => navigate("/products")}>
            Browse Products
          </button>
        </div>
      ) : (
        <div style={styles.grid}>
          {wishlist.map((product) => {
            const id = product.productId || product.id;
            const name = product.productName || product.name;
            const image = product.imageUrl || product.image;
            const price = product.price;

            return (
              <div key={id} style={styles.card}>
                <img
                  src={image || FALLBACK_IMAGE}
                  alt={name}
                  style={styles.image}
                  onError={(event) => {
                    event.currentTarget.src = FALLBACK_IMAGE;
                  }}
                />
                <h3 style={styles.productName}>{name}</h3>
                {product.discountRate > 0 && (
                  <span style={styles.discountBadge}>🏷️ -{product.discountRate}% OFF</span>
                )}
                <p style={styles.price}>
                  {product.discountRate > 0 && product.originalPrice && (
                    <span style={styles.originalPrice}>₺{Number(product.originalPrice).toFixed(2)} </span>
                  )}
                  {price ? `₺${Number(price).toFixed(2)}` : ""}
                </p>

                <div style={styles.actions}>
                  <button style={styles.cartBtn} onClick={() => handleAddToCart(product)}>
                    Add to Cart
                  </button>
                  <button style={styles.removeBtn} onClick={() => handleRemove(id)}>
                    Remove
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

const styles = {
  page: { padding: "32px", minHeight: "100vh", background: "linear-gradient(180deg, #f8f4ee 0%, #f3ece3 100%)" },
  title: { fontSize: "32px", marginBottom: "24px", color: "#4b2e2e" },
  emptyBox: { textAlign: "center", paddingTop: "60px" },
  empty: { fontSize: "18px", color: "#6b5b53", marginBottom: "16px" },
  grid: { display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "20px" },
  card: { border: "1px solid #e5d9ce", padding: "16px", borderRadius: "12px", textAlign: "center", backgroundColor: "white", boxShadow: "0 2px 8px rgba(0,0,0,0.06)" },
  image: { width: "100%", height: "220px", objectFit: "contain", objectPosition: "center", backgroundColor: "#ffffff", border: "1px solid #eee5dc", borderRadius: "8px", padding: "8px", boxSizing: "border-box" },
  productName: { fontSize: "15px", fontWeight: 600, color: "#4b2e2e", margin: "10px 0 4px" },
  price: { color: "#6b4f3b", fontWeight: 700, marginBottom: "12px" },
  actions: { display: "flex", gap: "8px", justifyContent: "center", flexWrap: "wrap" },
  cartBtn: { padding: "8px 14px", backgroundColor: "#4b2e2e", color: "white", border: "none", borderRadius: "8px", cursor: "pointer", fontSize: "13px" },
  removeBtn: { padding: "8px 14px", backgroundColor: "#dc2626", color: "white", border: "none", borderRadius: "8px", cursor: "pointer", fontSize: "13px" },
  button: { padding: "10px 20px", backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: "8px", cursor: "pointer" },
  discountBadge: { display: "inline-block", backgroundColor: "#dc2626", color: "white", fontSize: "11px", fontWeight: 700, padding: "2px 8px", borderRadius: "999px", marginBottom: "6px" },
  originalPrice: { textDecoration: "line-through", color: "#9ca3af", fontSize: "13px", marginRight: "4px" },
};

export default WishlistPage;