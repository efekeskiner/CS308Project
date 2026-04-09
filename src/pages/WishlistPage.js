import { useEffect, useState } from "react";
import { getWishlist, removeFromWishlist } from "../services/wishlist";

function WishlistPage() {
  const [wishlist, setWishlist] = useState([]);

  useEffect(() => {
    setWishlist(getWishlist());
  }, []);

  const handleRemove = (id) => {
    const updated = removeFromWishlist(id);
    setWishlist(updated);
  };

  return (
    <div style={styles.page}>
      <h1 style={styles.title}>My Wishlist</h1>

      {wishlist.length === 0 ? (
        <p style={styles.empty}>Your wishlist is empty</p>
      ) : (
        <div style={styles.grid}>
          {wishlist.map((product) => (
            <div key={product.id} style={styles.card}>
              <img src={product.image} alt={product.name} style={styles.image} />

              <h3>{product.name}</h3>
              <p>{product.author}</p>
              <p>{product.price} TL</p>

              <button
                style={styles.button}
                onClick={() => handleRemove(product.id)}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const styles = {
  page: {
    padding: "32px",
  },
  title: {
    fontSize: "32px",
    marginBottom: "20px",
  },
  empty: {
    fontSize: "18px",
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "20px",
  },
  card: {
    border: "1px solid #ddd",
    padding: "16px",
    borderRadius: "12px",
    textAlign: "center",
  },
  image: {
    width: "100%",
    height: "180px",
    objectFit: "cover",
    borderRadius: "8px",
  },
  button: {
    marginTop: "10px",
    padding: "10px",
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
  },
};

export default WishlistPage;