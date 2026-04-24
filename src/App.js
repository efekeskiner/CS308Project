import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import ProductList from "./pages/ProductList";
import PrivateRoute from "./components/PrivateRoute";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import WishlistPage from "./pages/WishlistPage";
import ProfilePage from "./pages/ProfilePage";
import OrdersPage from "./pages/OrdersPage";
import AdminPage from "./pages/AdminPage";
import { isLoggedIn, getCurrentUser, logout } from "./services/auth";
import { getCart } from "./services/cart";
import { useState, useEffect } from "react";

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(null);
  const [cartCount, setCartCount] = useState(0);

  useEffect(() => {
    setUser(isLoggedIn() ? getCurrentUser() : null);
    setCartCount(getCart().reduce((s, i) => s + i.quantity, 0));
  }, [location]);

  const handleLogout = () => {
    logout();
    setUser(null);
    navigate("/login");
  };

  const noNavRoutes = ["/login", "/register"];
  if (noNavRoutes.includes(location.pathname)) return null;

  return (
    <nav style={styles.navbar}>
      <span style={styles.brand} onClick={() => navigate("/products")}>
        Online Bookstore
      </span>
      <div style={styles.links}>
        <button style={styles.navButton} onClick={() => navigate("/products")}>Products</button>
        <button style={styles.navButton} onClick={() => navigate("/cart")}>
          🛒 Cart {cartCount > 0 && <span style={styles.badge}>{cartCount}</span>}
        </button>
        {user ? (
          <>
            <button style={styles.navButton} onClick={() => navigate("/orders")}>My Orders</button>
            <button style={styles.navButton} onClick={() => navigate("/wishlist")}>Wishlist</button>
            <button style={styles.navButton} onClick={() => navigate("/profile")}>
              👤 {user.name?.split(" ")[0] || "Profile"}
            </button>
            {(user.role === "SALES_MANAGER" || user.role === "PRODUCT_MANAGER") && (
              <button style={styles.navButton} onClick={() => navigate("/admin")}>Admin</button>
            )}
            <button style={{ ...styles.navButton, ...styles.logoutBtn }} onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <button style={styles.navButton} onClick={() => navigate("/login")}>Login</button>
            <button style={{ ...styles.navButton, ...styles.registerBtn }} onClick={() => navigate("/register")}>
              Register
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/products" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/products" element={<ProductList />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/wishlist" element={<WishlistPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/orders" element={<PrivateRoute><OrdersPage /></PrivateRoute>} />
        <Route path="/admin" element={<PrivateRoute><AdminPage /></PrivateRoute>} />
        <Route path="/checkout" element={<PrivateRoute><CheckoutPage /></PrivateRoute>} />
      </Routes>
    </Router>
  );
}

const styles = {
  navbar: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    padding: "12px 32px",
    backgroundColor: "#4b2e2e",
    boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
    position: "sticky",
    top: 0,
    zIndex: 100,
  },
  brand: {
    color: "white",
    fontSize: "20px",
    fontWeight: "bold",
    cursor: "pointer",
  },
  links: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    flexWrap: "wrap",
  },
  navButton: {
    backgroundColor: "transparent",
    color: "white",
    border: "1px solid rgba(255,255,255,0.3)",
    borderRadius: "8px",
    padding: "8px 14px",
    fontSize: "14px",
    cursor: "pointer",
    position: "relative",
  },
  badge: {
    backgroundColor: "#f97316",
    color: "white",
    borderRadius: "999px",
    padding: "1px 6px",
    fontSize: "11px",
    fontWeight: "bold",
    marginLeft: "4px",
  },
  logoutBtn: {
    borderColor: "#f87171",
    color: "#fca5a5",
  },
  registerBtn: {
    backgroundColor: "#6b4f3b",
    borderColor: "#6b4f3b",
  },
};

export default App;