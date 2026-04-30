const BASE_URL = "http://localhost:8080/api/products";

export async function getProducts() {
  const res = await fetch(BASE_URL);
  if (!res.ok) {
    throw new Error("Failed to fetch products");
  }
  return res.json();
}