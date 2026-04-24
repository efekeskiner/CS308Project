const API_URL = "http://localhost:8080/api/orders";

function getAuthHeaders() {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function getMyOrders() {
  const response = await fetch(API_URL, {
    headers: {
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch orders");
  }

  return response.json();
}