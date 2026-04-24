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

export async function getApprovedComments(productId) {
  const response = await fetch(`${API_URL}/products/${productId}/comments`);

  if (!response.ok) {
    throw new Error("Failed to fetch comments");
  }

  return response.json();
}

export async function submitComment(productId, content) {
  const response = await fetch(`${API_URL}/products/${productId}/comments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
    },
    body: JSON.stringify({ content }),
  });

  if (!response.ok) {
    throw new Error("Failed to submit comment");
  }

  return response.json();
}