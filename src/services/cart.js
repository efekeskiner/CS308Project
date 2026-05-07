const CART_KEY = "cart";

export function getCart() {
  try {
    const raw = localStorage.getItem(CART_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (error) {
    console.error("Failed to read cart from localStorage:", error);
    return [];
  }
}

export function saveCart(cartItems) {
  localStorage.setItem(CART_KEY, JSON.stringify(cartItems));
  window.dispatchEvent(new Event("cartUpdated"));
}

export function addToCart(product, quantityToAdd = 1) {
  const cart = getCart();

  const existingItem = cart.find((item) => item.id === product.id);
  const stockLimit = Number(product.quantityInStock) || Infinity;
  const safeQuantityToAdd = Math.max(1, Number(quantityToAdd) || 1);

  if (existingItem) {
    existingItem.quantity = Math.min(
      existingItem.quantity + safeQuantityToAdd,
      stockLimit
    );
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: Number(product.price) || 0,
      quantity: Math.min(safeQuantityToAdd, stockLimit),
      imageUrl: product.image || product.imageUrl || "",
      inStock: product.inStock !== false,
      quantityInStock: product.quantityInStock,
    });
  }

  saveCart(cart);
  return cart;
}

export function updateCartItemQuantity(productId, newQuantity) {
  let cart = getCart();

  if (newQuantity <= 0) {
    cart = cart.filter((item) => item.id !== productId);
  } else {
    cart = cart.map((item) =>
      item.id === productId ? { ...item, quantity: newQuantity } : item
    );
  }

  saveCart(cart);
  return cart;
}

export function removeFromCart(productId) {
  const cart = getCart().filter((item) => item.id !== productId);
  saveCart(cart);
  return cart;
}

export function clearCart() {
  localStorage.removeItem(CART_KEY);
  window.dispatchEvent(new Event("cartUpdated"));
}

export function getCartTotal(cartItems) {
  return cartItems.reduce(
    (total, item) => total + Number(item.price) * Number(item.quantity),
    0
  );
}