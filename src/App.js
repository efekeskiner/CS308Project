import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import ProductList from "./pages/ProductList";
import PrivateRoute from "./components/PrivateRoute";
import CartPage from "./pages/CartPage";
import CheckoutPage from "./pages/CheckoutPage";

function App() {
    return (
          <Router>
            <Routes>
              <Route path="/" element={<Navigate to="/products" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/cart" element={<CartPage />} />
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

export default App;
