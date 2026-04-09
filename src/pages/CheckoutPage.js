import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./CheckoutPage.css";
import { getCart, clearCart, getCartTotal } from "../services/cart";
import { getCurrentUser } from "../services/auth";

function CheckoutPage() {
  const navigate = useNavigate();
  const cartItems = getCart();
  const currentUser = getCurrentUser();

  const [address, setAddress] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [orderPlaced, setOrderPlaced] = useState(false);
  const [mockInvoiceId, setMockInvoiceId] = useState(null);
  const [error, setError] = useState("");

  const totalPrice = useMemo(() => getCartTotal(cartItems), [cartItems]);

  const handlePlaceOrder = (e) => {
    e.preventDefault();

    if (cartItems.length === 0) {
      setError("Your cart is empty.");
      return;
    }

    if (!address.trim() || !cardNumber.trim() || !expiry.trim() || !cvv.trim()) {
      setError("Please fill in all checkout fields.");
      return;
    }

    setError("");

    const mockOrderPayload = {
      items: cartItems.map((item) => ({
        productId: item.id,
        quantity: item.quantity,
      })),
      creditCard: {
        number: cardNumber,
        expiry,
        cvv,
      },
    };

    console.log("Mock checkout payload:", mockOrderPayload);

    const generatedInvoiceId = Math.floor(Math.random() * 100000) + 1;
    setMockInvoiceId(generatedInvoiceId);
    setOrderPlaced(true);
    clearCart();
  };

  if (orderPlaced) {
    return (
      <div className="checkout-page">
        <div className="checkout-container success-card">
          <p className="checkout-brand">Online Bookstore</p>
          <h1>Order Placed Successfully</h1>
          <p className="checkout-subtitle">
            Your order has been created in the mock frontend flow.
          </p>

          <div className="invoice-box">
            <p><strong>Customer:</strong> {currentUser?.name || "Customer"}</p>
            <p><strong>Invoice ID:</strong> {mockInvoiceId}</p>
            <p><strong>Total Paid:</strong> ₺{totalPrice.toFixed(2)}</p>
            <p><strong>Status:</strong> PROCESSING</p>
          </div>

          <div className="success-actions">
            <button className="primary-btn" onClick={() => navigate("/products")}>
              Back to Products
            </button>
            <button className="outline-btn" onClick={() => navigate("/orders")}>
              Go to Orders Later
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <div className="checkout-container">
        <div className="checkout-header">
          <div>
            <p className="checkout-brand">Online Bookstore</p>
            <h1>Checkout</h1>
            <p className="checkout-subtitle">
              Review your order, confirm your address, and enter payment details.
            </p>
          </div>

          <button className="outline-btn" onClick={() => navigate("/cart")}>
            Back to Cart
          </button>
        </div>

        {cartItems.length === 0 ? (
          <div className="empty-checkout">
            <h2>Your cart is empty</h2>
            <p>Add some books before trying to checkout.</p>
            <button className="primary-btn" onClick={() => navigate("/products")}>
              Browse Products
            </button>
          </div>
        ) : (
          <div className="checkout-layout">
            <form className="checkout-form-card" onSubmit={handlePlaceOrder}>
              <h2>Shipping & Payment</h2>

              <label htmlFor="customerName">Customer</label>
              <input
                id="customerName"
                type="text"
                value={currentUser?.name || ""}
                disabled
              />

              <label htmlFor="address">Delivery Address</label>
              <textarea
                id="address"
                rows="4"
                placeholder="Enter your full address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />

              <label htmlFor="cardNumber">Card Number</label>
              <input
                id="cardNumber"
                type="text"
                placeholder="1234 5678 9012 3456"
                value={cardNumber}
                onChange={(e) => setCardNumber(e.target.value)}
              />

              <div className="payment-row">
                <div>
                  <label htmlFor="expiry">Expiry</label>
                  <input
                    id="expiry"
                    type="text"
                    placeholder="MM/YY"
                    value={expiry}
                    onChange={(e) => setExpiry(e.target.value)}
                  />
                </div>

                <div>
                  <label htmlFor="cvv">CVV</label>
                  <input
                    id="cvv"
                    type="password"
                    placeholder="123"
                    value={cvv}
                    onChange={(e) => setCvv(e.target.value)}
                  />
                </div>
              </div>

              {error && <p className="error-text">{error}</p>}

              <button type="submit" className="primary-btn full-width-btn">
                Place Order
              </button>
            </form>

            <div className="checkout-summary-card">
              <h2>Order Summary</h2>

              <div className="summary-items">
                {cartItems.map((item) => (
                  <div key={item.id} className="summary-item">
                    <div>
                      <p className="item-name">{item.name}</p>
                      <p className="item-meta">
                        Qty: {item.quantity} × ₺{Number(item.price).toFixed(2)}
                      </p>
                    </div>
                    <p className="item-total">
                      ₺{(Number(item.price) * Number(item.quantity)).toFixed(2)}
                    </p>
                  </div>
                ))}
              </div>

              <div className="summary-total">
                <span>Total</span>
                <span>₺{totalPrice.toFixed(2)}</span>
              </div>

              <p className="summary-note">
                This page is frontend-only for now. Later, it will call the order API with
                items and credit card info.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default CheckoutPage;