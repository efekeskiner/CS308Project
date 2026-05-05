import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import ProductList from "./pages/ProductList";
import PrivateRoute from "./components/PrivateRoute";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import WishlistPage from "./pages/WishlistPage";
import ProfilePage from "./pages/ProfilePage";
import ProductDetailPage from "./pages/ProductDetailPage";
import OrdersPage from "./pages/OrdersPage";
import AdminPage from "./pages/AdminPage";
import { isLoggedIn, getCurrentUser, logout } from "./services/auth";
import { getCart } from "./services/cart";
import { useState, useEffect } from "react";
import "./App.css";

const isManagerUser = (user) =>
  user?.role === "PRODUCT_MANAGER" || user?.role === "SALES_MANAGER";

const isCustomerUser = (user) => user?.role === "CUSTOMER";

function CustomerOnlyRoute({ children }) {
  const user = isLoggedIn() ? getCurrentUser() : null;

  if (isManagerUser(user)) {
    return <Navigate to="/admin" />;
  }

  return children;
}

function CustomerPrivateRoute({ children }) {
  const user = isLoggedIn() ? getCurrentUser() : null;

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (!isCustomerUser(user)) {
    return <Navigate to="/admin" />;
  }

  return children;
}

function ManagerRoute({ children }) {
  const user = isLoggedIn() ? getCurrentUser() : null;

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (!isManagerUser(user)) {
    return <Navigate to="/products" />;
  }

  return children;
}

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(null);
  const [cartCount, setCartCount] = useState(0);
  const [isCartBouncing, setIsCartBouncing] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  useEffect(() => {
    const updateNavbar = () => {
      const newCartCount = getCart().reduce((s, i) => s + i.quantity, 0);

      setUser(isLoggedIn() ? getCurrentUser() : null);

      setCartCount((oldCartCount) => {
        if (newCartCount > oldCartCount) {
          setIsCartBouncing(true);

          setTimeout(() => {
            setIsCartBouncing(false);
          }, 450);
        }

        return newCartCount;
      });
    };

    updateNavbar();

    window.addEventListener("cartUpdated", updateNavbar);

    return () => {
      window.removeEventListener("cartUpdated", updateNavbar);
    };
  }, [location]);

  const handleLogout = () => {
    setProfileMenuOpen(false);
    logout();
    setUser(null);
    navigate("/login");
  };

  const noNavRoutes = ["/login", "/register"];
  if (noNavRoutes.includes(location.pathname)) return null;

  const isCustomer = !user || isCustomerUser(user);
  const isManager = isManagerUser(user);

  return (
    <nav style={styles.navbar}>
      <span style={styles.brand} onClick={() => navigate("/products")}>
        Online Bookstore
      </span>

      <div style={styles.links}>
        <button style={styles.navButton} onClick={() => navigate("/products")}>
          Products
        </button>

        {isCustomer && (
          <button
            className={isCartBouncing ? "cart-bounce" : ""}
            style={styles.navButton}
            onClick={() => navigate("/cart")}
          >
            🛒 Cart {cartCount > 0 && <span style={styles.badge}>{cartCount}</span>}
          </button>
        )}

        {user ? (
          <>
            {isCustomerUser(user) && (
              <>
                <button style={styles.navButton} onClick={() => navigate("/orders")}>
                  My Orders
                </button>

                <button style={styles.navButton} onClick={() => navigate("/wishlist")}>
                  Wishlist
                </button>
              </>
            )}

            <div style={styles.profileMenuWrapper}>
              <button
                style={{ ...styles.navButton, ...styles.profileTrigger }}
                onClick={() => setProfileMenuOpen((prev) => !prev)}
              >
                👤 {user.name?.split(" ")[0] || "Profile"} ▾
              </button>

              {profileMenuOpen && (
                <div style={styles.dropdownMenu}>
                  <button
                    style={styles.dropdownItem}
                    onClick={() => {
                      setProfileMenuOpen(false);
                      navigate("/profile");
                    }}
                  >
                    Profile
                  </button>

                  {isManager && (
                    <button
                      style={styles.dropdownItem}
                      onClick={() => {
                        setProfileMenuOpen(false);
                        navigate("/admin");
                      }}
                    >
                      Admin
                    </button>
                  )}

                  <button
                    style={styles.dropdownItem}
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <button style={styles.navButton} onClick={() => navigate("/login")}>
              Login
            </button>

            <button
              style={{ ...styles.navButton, ...styles.registerBtn }}
              onClick={() => navigate("/register")}
            >
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
        <Route path="/products/:id" element={<ProductDetailPage />} />

        <Route
          path="/cart"
          element={
            <CustomerOnlyRoute>
              <CartPage />
            </CustomerOnlyRoute>
          }
        />

        <Route
          path="/wishlist"
          element={
            <CustomerOnlyRoute>
              <WishlistPage />
            </CustomerOnlyRoute>
          }
        />

        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <ProfilePage />
            </PrivateRoute>
          }
        />

        <Route
          path="/orders"
          element={
            <CustomerPrivateRoute>
              <OrdersPage />
            </CustomerPrivateRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ManagerRoute>
              <AdminPage />
            </ManagerRoute>
          }
        />

        <Route
          path="/checkout"
          element={
            <CustomerPrivateRoute>
              <CheckoutPage />
            </CustomerPrivateRoute>
          }
        />
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
  profileMenuWrapper: {
    position: "relative",
  },
  profileTrigger: {
    minWidth: "170px",
  },
  dropdownMenu: {
    position: "absolute",
    top: "calc(100% + 8px)",
    right: 0,
    backgroundColor: "white",
    border: "1px solid rgba(0,0,0,0.1)",
    borderRadius: "12px",
    boxShadow: "0 12px 24px rgba(0,0,0,0.12)",
    overflow: "hidden",
    zIndex: 20,
    minWidth: "160px",
  },
  dropdownItem: {
    width: "100%",
    textAlign: "left",
    backgroundColor: "transparent",
    border: "none",
    color: "#3f3f46",
    padding: "12px 14px",
    cursor: "pointer",
    fontSize: "14px",
  },
  registerBtn: {
    backgroundColor: "#6b4f3b",
    borderColor: "#6b4f3b",
  },
};

export default App;