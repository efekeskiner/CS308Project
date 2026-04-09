import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import ProductList from "./pages/ProductList";
import PrivateRoute from "./components/PrivateRoute";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";
import WishlistPage from "./pages/WishlistPage";

function App() {
    return (
          <Router>
             <div style={styles.navbar}>
  <button
    style={styles.navButton}
    onClick={() => window.location.href = "/products"}
  >
    Products
  </button>

  <button
    style={styles.navButton}
    onClick={() => window.location.href = "/wishlist"}
  >
    Wishlist
  </button>
</div>
            <Routes>
              <Route path="/" element={<Navigate to="/products" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/cart" element={<CartPage />} />

        <Route path="/wishlist" element={<WishlistPage />} />

        <Route
          path="/checkout"
          element={
            <PrivateRoute>
              <CheckoutPage />
            </PrivateRoute>
          }
        />

        <Route path="/products" element={<ProductList />} />

        {/* <Route
          path="/products"
          element={
                        <PrivateRoute>
                          <ProductList />
            </PrivateRoute>
}
        /> */}

          </Routes>
          </Router>
  );
}
const styles = {
  navbar: {
    display: "flex",
    gap: "10px",
    padding: "16px 32px 0 32px",
    justifyContent: "flex-end",
  },
  navButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
  },
};
export default App;
