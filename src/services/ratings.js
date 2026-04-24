const API_URL = "http://localhost:8080/api";

function getAuthHeaders() {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function getProductRating(productId) {
  const response = await fetch(`${API_URL}/products/${productId}/ratings`);

  if (!response.ok) {
    throw new Error("Failed to fetch rating");
  }

  return response.json();
}

export async function submitRating(productId, score) {
  const response = await fetch(`${API_URL}/products/${productId}/ratings`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
    },
    body: JSON.stringify({ score }),
  });

  if (!response.ok) {
    throw new Error("Failed to submit rating");
  }

  return response.json();
}