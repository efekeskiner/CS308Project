const WISHLIST_KEY = "wishlist";

export function getWishlist() {
  const wishlist = localStorage.getItem(WISHLIST_KEY);
  return wishlist ? JSON.parse(wishlist) : [];
}

export function saveWishlist(wishlist) {
  localStorage.setItem(WISHLIST_KEY, JSON.stringify(wishlist));
}

export function isInWishlist(productId) {
  const wishlist = getWishlist();
  return wishlist.some((item) => item.id === productId);
}

export function addToWishlist(product) {
  const wishlist = getWishlist();

  const exists = wishlist.some((item) => item.id === product.id);
  if (exists) return wishlist;

  const updatedWishlist = [...wishlist, product];
  saveWishlist(updatedWishlist);
  return updatedWishlist;
}

export function removeFromWishlist(productId) {
  const wishlist = getWishlist();
  const updatedWishlist = wishlist.filter((item) => item.id !== productId);
  saveWishlist(updatedWishlist);
  return updatedWishlist;
}

export function toggleWishlist(product) {
  if (isInWishlist(product.id)) {
    return removeFromWishlist(product.id);
  }
  return addToWishlist(product);
}

export function clearWishlist() {
  localStorage.removeItem(WISHLIST_KEY);
}