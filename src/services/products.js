const API_URL = "http://localhost:8080/api/products";

async function getProducts() {
  const response = await fetch(API_URL);

  if (!response.ok) {
    throw new Error("Failed to fetch products");
  }

  return response.json();
}

export async function getProductById(productId) {
  const response = await fetch(`${API_URL}/${productId}`);

  if (!response.ok) {
    throw new Error("Failed to fetch product details");
  }

  return response.json();
}

export default getProducts;