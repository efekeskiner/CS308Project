import { Navigate } from "react-router-dom";
import { isLoggedIn } from "../services/auth";

/**
 * PrivateRoute - Login gerektiren sayfalari korur.
 * Kullanim: <PrivateRoute><MyPage /></PrivateRoute>
 * Giris yapilmamissa /login sayfasina yonlendirir.
 */
function PrivateRoute({ children }) {
    return isLoggedIn() ? children : <Navigate to="/login" replace />;
}

export default PrivateRoute;
