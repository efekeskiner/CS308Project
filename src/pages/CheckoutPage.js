import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./CheckoutPage.css";
import { getCart, clearCart, getCartTotal } from "../services/cart";
import { getCurrentUser } from "../services/auth";
import { placeOrder } from "../services/orders";
import { downloadInvoicePdf } from "../services/invoices";

function CheckoutPage() {
  const navigate = useNavigate();
  const cartItems = getCart();
  const currentUser = getCurrentUser();

  const [address, setAddress] = useState(currentUser?.homeAddress || "");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [orderPlaced, setOrderPlaced] = useState(false);
  const [placedOrder, setPlacedOrder] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const totalPrice = useMemo(() => getCartTotal(cartItems), [cartItems]);

  const handlePlaceOrder = async (e) => {
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
    setLoading(true);

    try {
      const items = cartItems.map((item) => ({
        productId: item.id,
        quantity: item.quantity,
      }));
      const creditCard = { number: cardNumber.replace(/\s/g, ""), expiry, cvv };

      const order = await placeOrder(items, creditCard, address);
      setPlacedOrder(order);
      setOrderPlaced(true);
      clearCart();
    } catch (err) {
      setError(err.message || "Order could not be placed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (orderPlaced && placedOrder) {
    const invoiceItems = Array.isArray(placedOrder.items) ? placedOrder.items : [];

    return (
      <div className="checkout-page">
        <div className="checkout-container success-card">
          <p className="checkout-brand">Online Bookstore</p>
          <h1>Order Placed Successfully!</h1>
          <p className="checkout-subtitle">
            Your order is being processed. A copy of this invoice has been emailed to you.
          </p>

          <div className="invoice-box">
            <div className="invoice-header-row">
              <div>
                <p><strong>Customer:</strong> {currentUser?.name || "Customer"}</p>
                <p><strong>Order ID:</strong> #{placedOrder.id}</p>
                <p><strong>Invoice ID:</strong> #{placedOrder.invoiceId || "—"}</p>
                <p><strong>Status:</strong> {placedOrder.status || "PROCESSING"}</p>
              </div>
              {placedOrder.invoiceId && (
                <button
                  className="pdf-download-btn"
                  onClick={() => downloadInvoicePdf(placedOrder.invoiceId, placedOrder.id)}
                >
                  Download Invoice PDF
                </button>
              )}
            </div>

            {invoiceItems.length > 0 && (
              <table className="invoice-items-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Qty</th>
                    <th>Unit Price</th>
                    <th>Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {invoiceItems.map((item) => (
                    <tr key={item.id}>
                      <td>{item.productName}</td>
                      <td>{item.quantity}</td>
                      <td>₺{Number(item.unitPrice).toFixed(2)}</td>
                      <td>₺{(Number(item.unitPrice) * Number(item.quantity)).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan={3}><strong>Total</strong></td>
                    <td><strong>₺{Number(placedOrder.totalPrice).toFixed(2)}</strong></td>
                  </tr>
                </tfoot>
              </table>
            )}

            {invoiceItems.length === 0 && (
              <p><strong>Total Paid:</strong> ₺{Number(placedOrder.totalPrice).toFixed(2)}</p>
            )}
          </div>

          <div className="success-actions">
            <button className="primary-btn" onClick={() => navigate("/orders")}>
              View My Orders
            </button>
            <button className="outline-btn" onClick={() => navigate("/products")}>
              Continue Shopping
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

              <button type="submit" className="primary-btn full-width-btn" disabled={loading}>
                {loading ? "Placing Order..." : "Place Order"}
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
                Payment is securely processed. Your invoice will be emailed after order confirmation.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default CheckoutPage;