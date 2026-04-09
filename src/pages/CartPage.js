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

function CartPage() {
  const [cartItems, setCartItems] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    setCartItems(getCart());
  }, []);

  const totalPrice = useMemo(() => getCartTotal(cartItems), [cartItems]);

  const handleIncrease = (productId, currentQuantity) => {
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
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.name}
                          className="cart-item-image"
                        />
                      ) : (
                        <div className="cart-item-image placeholder">No Image</div>
                      )}
                    </div>

                    <div className="cart-item-middle">
                      <h3>{item.name}</h3>
                      <p className="price">Unit Price: ${Number(item.price).toFixed(2)}</p>
                      <p className={item.inStock ? "stock in-stock" : "stock out-of-stock"}>
                        {item.inStock ? "In Stock" : "Out of Stock"}
                      </p>

                      <div className="quantity-controls">
                        <button onClick={() => handleDecrease(item.id, item.quantity)}>-</button>
                        <span>{item.quantity}</span>
                        <button onClick={() => handleIncrease(item.id, item.quantity)}>+</button>
                      </div>
                    </div>

                    <div className="cart-item-right">
                      <p className="subtotal-label">Subtotal</p>
                      <p className="subtotal-value">${subtotal.toFixed(2)}</p>
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
                <span>${totalPrice.toFixed(2)}</span>
              </div>

              <button className="primary-btn" onClick={handleProceedToCheckout}>
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