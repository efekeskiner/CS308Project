import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./CartPage.css";
import {
  getCart,
  updateCartItemQuantity,
  removeFromCart,
  clearCart,
  getCartTotal,
} from "../services/cart";
import { getProductById } from "../services/products";

const FALLBACK_IMAGE =
  "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='420' viewBox='0 0 300 420'%3E%3Crect width='300' height='420' fill='%23f3ece3'/%3E%3Crect x='45' y='55' width='210' height='310' rx='16' fill='%23ffffff' stroke='%23d1c7bc' stroke-width='3'/%3E%3Ctext x='150' y='195' text-anchor='middle' font-family='Arial' font-size='24' fill='%236b4f3b'%3ENo Image%3C/text%3E%3Ctext x='150' y='230' text-anchor='middle' font-family='Arial' font-size='16' fill='%238b7b72'%3EBook Cover%3C/text%3E%3C/svg%3E";

const getCartItemImage = (item) => {
  if (item.imageUrl) {
    return item.imageUrl;
  }

  if (item.serialNumber) {
    return `https://covers.openlibrary.org/b/isbn/${item.serialNumber}-L.jpg`;
  }

  return FALLBACK_IMAGE;
};

function CartPage() {
  const [cartItems, setCartItems] = useState([]);
  const [stockMap, setStockMap] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    setCartItems(getCart());
  }, []);

  useEffect(() => {
    if (cartItems.length === 0) {
      setStockMap({});
      return;
    }
    Promise.all(cartItems.map((item) => getProductById(item.id)))
      .then((products) => {
        const map = {};
        products.forEach((p) => { map[p.id] = p.quantityInStock ?? 0; });
        setStockMap(map);
      })
      .catch(() => {});
  }, [cartItems.length]);

  const totalPrice = useMemo(() => getCartTotal(cartItems), [cartItems]);

  const hasStockIssues = cartItems.some((item) => {
    const available = stockMap[item.id];
    return available !== undefined && (available === 0 || item.quantity > available);
  });

  const handleIncrease = (productId, currentQuantity) => {
    const available = stockMap[productId];
    if (available !== undefined && currentQuantity >= available) return;
    const updatedCart = updateCartItemQuantity(productId, currentQuantity + 1);
    setCartItems(updatedCart);
  };

  const handleDecrease = (productId, currentQuantity) => {
    const updatedCart = updateCartItemQuantity(productId, currentQuantity - 1);
    setCartItems(updatedCart);
  };

  const handleRemove = (productId) => {
    const updatedCart = removeFromCart(productId);
    setCartItems(updatedCart);
  };

  const handleClearCart = () => {
    clearCart();
    setCartItems([]);
  };

  const isAuthenticated = () => {
    return (
      !!localStorage.getItem("accessToken") ||
      !!localStorage.getItem("token") ||
      !!localStorage.getItem("user")
    );
  };

  const handleProceedToCheckout = () => {
    if (cartItems.length === 0) return;
    if (!isAuthenticated()) {
      navigate("/login", { state: { from: "/checkout" } });
      return;
    }
    if (hasStockIssues) return;
    navigate("/checkout");
  };

  return (
    <div className="cart-page">
      <div className="cart-container">
        <div className="cart-header">
          <div>
            <p className="cart-brand">Online Bookstore</p>
            <h1>Your Cart</h1>
            <p className="cart-subtitle">
              Review your selected books, update quantities, and continue to checkout.
            </p>
          </div>

          {cartItems.length > 0 && (
            <button className="secondary-btn" onClick={handleClearCart}>
              Clear Cart
            </button>
          )}
        </div>

        {cartItems.length === 0 ? (
          <div className="empty-cart">
            <h2>Your cart is empty</h2>
            <p>Add some books before proceeding to checkout.</p>
            <button className="primary-btn" onClick={() => navigate("/products")}>
              Browse Products
            </button>
          </div>
        ) : (
          <div className="cart-layout">
            <div className="cart-items">
              {cartItems.map((item) => {
                const subtotal = Number(item.price) * Number(item.quantity);

                return (
                  <div key={item.id} className="cart-item-card">
                    <div className="cart-item-left">
                      <img
                        src={getCartItemImage(item)}
                        alt={item.name}
                        className="cart-item-image"
                        onError={(event) => {
                          event.currentTarget.src = FALLBACK_IMAGE;
                        }}
                      />
                    </div>

                    <div className="cart-item-middle">
                      <h3>{item.name}</h3>
                      <p className="price">Unit Price: ₺{Number(item.price).toFixed(2)}</p>
                      {(() => {
                        const available = stockMap[item.id];
                        if (available === undefined) return null;
                        if (available === 0)
                          return <p className="stock out-of-stock">Out of Stock</p>;
                        if (item.quantity > available)
                          return <p className="stock out-of-stock">Only {available} available — reduce quantity</p>;
                        return <p className="stock in-stock">In Stock</p>;
                      })()}

                      <div className="quantity-controls">
                        <button onClick={() => handleDecrease(item.id, item.quantity)}>-</button>
                        <span>{item.quantity}</span>
                        <button onClick={() => handleIncrease(item.id, item.quantity)}>+</button>
                      </div>
                    </div>

                    <div className="cart-item-right">
                      <p className="subtotal-label">Subtotal</p>
                      <p className="subtotal-value">₺{subtotal.toFixed(2)}</p>
                      <button
                        className="remove-btn"
                        onClick={() => handleRemove(item.id)}
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="cart-summary">
              <h2>Order Summary</h2>

              <div className="summary-row">
                <span>Items</span>
                <span>{cartItems.reduce((sum, item) => sum + item.quantity, 0)}</span>
              </div>

              <div className="summary-row">
                <span>Total</span>
                <span>₺{totalPrice.toFixed(2)}</span>
              </div>

              {hasStockIssues && (
                <p className="stock out-of-stock" style={{ marginBottom: "8px", fontSize: "13px" }}>
                  Some items have stock issues. Fix quantities to continue.
                </p>
              )}
              <button
                className="primary-btn"
                onClick={handleProceedToCheckout}
                disabled={hasStockIssues}
              >
                Proceed to Checkout
              </button>

              <button className="outline-btn" onClick={() => navigate("/products")}>
                Continue Shopping
              </button>

              <p className="summary-note">
                Cart is kept in localStorage. Checkout requires authentication.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default CartPage;