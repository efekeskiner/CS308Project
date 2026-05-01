const API_URL = "http://localhost:8080/api/orders";

function getAuthHeaders() {
  const token = localStorage.getItem("accessToken");
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}

export async function getMyOrders() {
  const response = await fetch(API_URL, {
    headers: { ...getAuthHeaders() },
  });
  if (!response.ok) throw new Error("Failed to fetch orders");
  return response.json();
}

export async function placeOrder(items, creditCard) {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
    },
    body: JSON.stringify({ items, creditCard }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "Failed to place order");
  }
  return response.json();
}

export async function cancelOrder(orderId) {
  const response = await fetch(`${API_URL}/${orderId}/cancel`, {
    method: "PUT",
    headers: { ...getAuthHeaders() },
  });
  if (!response.ok) throw new Error("Failed to cancel order");
  return response.json();
}