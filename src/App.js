import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration";
import ProductList from "./pages/ProductList";
import PrivateRoute from "./components/PrivateRoute";

function App() {
    return (
          <Router>
            <Routes>
              <Route path="/" element={<Navigate to="/products" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route
          path="/products"
          element={
                        <PrivateRoute>
                          <ProductList />
            </PrivateRoute>
}
        />
          </Routes>
          </Router>
  );
}

export default App;
