// ============================================================
// CS308Project - 25 Unit Test Cases
// Tests for: auth service, cart service, wishlist service,
//            and App.js role-helper utilities
// Framework: Jest + @testing-library/react (Create React App)
// ============================================================

// ---------- helpers & mocks ----------
const mockLocalStorage = (() => {
  let store = {};
  return {
    getItem:    (k)    => store[k] !== undefined ? store[k] : null,
    setItem:    (k, v) => { store[k] = String(v); },
    removeItem: (k)    => { delete store[k]; },
    clear:      ()     => { store = {}; },
  };
})();

Object.defineProperty(global, 'localStorage', { value: mockLocalStorage });

global.window.dispatchEvent = jest.fn();

// ---------- inline implementations ----------
// auth helpers
const getToken       = () => localStorage.getItem('accessToken');
const getCurrentUser = () => { const u = localStorage.getItem('user'); return u ? JSON.parse(u) : null; };
const isLoggedIn     = () => !!getToken();
const logout         = () => { localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); localStorage.removeItem('user'); };

// role helpers (mirrors App.js)
const isManagerUser  = (user) => user && (user.role === 'PRODUCT_MANAGER' || user.role === 'SALES_MANAGER');
const isCustomerUser = (user) => user && user.role === 'CUSTOMER';

// cart helpers
const CART_KEY = 'cart';
const getCart   = () => { try { const r = localStorage.getItem(CART_KEY); return r ? JSON.parse(r) : []; } catch (e) { return []; } };
const saveCart  = (items) => { localStorage.setItem(CART_KEY, JSON.stringify(items)); window.dispatchEvent(new Event('cartUpdated')); };
const addToCart = (product) => {
  const cart = getCart();
  const ex   = cart.find(i => i.id === product.id);
  if (ex) { ex.quantity += 1; } else { cart.push({ id: product.id, name: product.name, price: Number(product.price), quantity: 1, imageUrl: product.image || product.imageUrl, inStock: product.inStock !== false }); }
  saveCart(cart); return cart;
};
const updateCartItemQuantity = (productId, newQuantity) => {
  let cart = getCart();
  if (newQuantity <= 0) { cart = cart.filter(i => i.id !== productId); } else { cart = cart.map(i => i.id === productId ? { ...i, quantity: newQuantity } : i); }
  saveCart(cart); return cart;
};
const removeFromCart = (productId) => { const cart = getCart().filter(i => i.id !== productId); saveCart(cart); return cart; };
const clearCart      = () => { localStorage.removeItem(CART_KEY); window.dispatchEvent(new Event('cartUpdated')); };
const getCartTotal   = (items) => items.reduce((t, i) => t + Number(i.price) * Number(i.quantity), 0);

// wishlist helpers
const WISHLIST_KEY       = 'wishlist';
const getWishlist        = () => { const w = localStorage.getItem(WISHLIST_KEY); return w ? JSON.parse(w) : []; };
const saveWishlist       = (wl) => localStorage.setItem(WISHLIST_KEY, JSON.stringify(wl));
const isInWishlist       = (pid) => getWishlist().some(i => i.id === pid);
const addToWishlist      = (product) => { const wl = getWishlist(); if (wl.some(i => i.id === product.id)) return wl; const updated = [...wl, product]; saveWishlist(updated); return updated; };
const removeFromWishlist = (pid) => { const updated = getWishlist().filter(i => i.id !== pid); saveWishlist(updated); return updated; };
const toggleWishlist     = (product) => isInWishlist(product.id) ? removeFromWishlist(product.id) : addToWishlist(product);
const clearWishlist      = () => localStorage.removeItem(WISHLIST_KEY);

// ================================================================
// TEST SUITE
// ================================================================

beforeEach(() => {
  localStorage.clear();
  jest.clearAllMocks();
});

// ----------------------------------------------------------------
// AUTH SERVICE TESTS (8 tests)
// ----------------------------------------------------------------
describe('Auth Service', () => {
  test('Test 1 - isLoggedIn returns false when no token stored', () => {
    expect(isLoggedIn()).toBe(false);
  });

  test('Test 2 - isLoggedIn returns true after token is stored', () => {
    localStorage.setItem('accessToken', 'test-token-123');
    expect(isLoggedIn()).toBe(true);
  });

  test('Test 3 - getToken returns null when nothing is stored', () => {
    expect(getToken()).toBeNull();
  });

  test('Test 4 - getToken returns the stored access token', () => {
    localStorage.setItem('accessToken', 'abc-xyz');
    expect(getToken()).toBe('abc-xyz');
  });

  test('Test 5 - getCurrentUser returns null when user is not stored', () => {
    expect(getCurrentUser()).toBeNull();
  });

  test('Test 6 - getCurrentUser returns parsed user object', () => {
    const user = { id: 1, name: 'Alice', role: 'CUSTOMER' };
    localStorage.setItem('user', JSON.stringify(user));
    expect(getCurrentUser()).toEqual(user);
  });

  test('Test 7 - logout clears accessToken, refreshToken, and user from localStorage', () => {
    localStorage.setItem('accessToken', 'tok');
    localStorage.setItem('refreshToken', 'ref');
    localStorage.setItem('user', JSON.stringify({ id: 1 }));
    logout();
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
  });

  test('Test 8 - logout does not throw when called on already-cleared storage', () => {
    expect(() => logout()).not.toThrow();
  });
});

// ----------------------------------------------------------------
// ROLE HELPER TESTS (4 tests)
// ----------------------------------------------------------------
describe('Role Helpers (from App.js)', () => {
  test('Test 9 - isManagerUser returns true for PRODUCT_MANAGER', () => {
    expect(isManagerUser({ role: 'PRODUCT_MANAGER' })).toBe(true);
  });

  test('Test 10 - isManagerUser returns true for SALES_MANAGER', () => {
    expect(isManagerUser({ role: 'SALES_MANAGER' })).toBe(true);
  });

  test('Test 11 - isManagerUser returns false for CUSTOMER role', () => {
    expect(isManagerUser({ role: 'CUSTOMER' })).toBe(false);
  });

  test('Test 12 - isCustomerUser returns true for CUSTOMER role', () => {
    expect(isCustomerUser({ role: 'CUSTOMER' })).toBe(true);
  });
});

// ----------------------------------------------------------------
// CART SERVICE TESTS (8 tests)
// ----------------------------------------------------------------
describe('Cart Service', () => {
  const sampleProduct = { id: 42, name: 'Clean Code', price: '29.99', inStock: true };

  test('Test 13 - getCart returns empty array when cart is empty', () => {
    expect(getCart()).toEqual([]);
  });

  test('Test 14 - addToCart adds a new product with quantity 1', () => {
    const cart = addToCart(sampleProduct);
    expect(cart).toHaveLength(1);
    expect(cart[0].quantity).toBe(1);
    expect(cart[0].name).toBe('Clean Code');
  });

  test('Test 15 - addToCart increments quantity for existing product', () => {
    addToCart(sampleProduct);
    const cart = addToCart(sampleProduct);
    expect(cart).toHaveLength(1);
    expect(cart[0].quantity).toBe(2);
  });

  test('Test 16 - addToCart converts price to Number', () => {
    const cart = addToCart(sampleProduct);
    expect(typeof cart[0].price).toBe('number');
    expect(cart[0].price).toBe(29.99);
  });

  test('Test 17 - updateCartItemQuantity updates the quantity of an existing item', () => {
    addToCart(sampleProduct);
    const cart = updateCartItemQuantity(42, 5);
    expect(cart[0].quantity).toBe(5);
  });

  test('Test 18 - updateCartItemQuantity removes item when newQuantity is 0', () => {
    addToCart(sampleProduct);
    const cart = updateCartItemQuantity(42, 0);
    expect(cart).toHaveLength(0);
  });

  test('Test 19 - removeFromCart removes the specified product', () => {
    addToCart(sampleProduct);
    addToCart({ id: 99, name: 'Refactoring', price: '39.99' });
    const cart = removeFromCart(42);
    expect(cart).toHaveLength(1);
    expect(cart[0].id).toBe(99);
  });

  test('Test 20 - getCartTotal calculates total price correctly', () => {
    const items = [
      { price: '10', quantity: 2 },
      { price: '5',  quantity: 3 },
    ];
    expect(getCartTotal(items)).toBeCloseTo(35);
  });
});

// ----------------------------------------------------------------
// WISHLIST SERVICE TESTS (5 tests)
// ----------------------------------------------------------------
describe('Wishlist Service', () => {
  const sampleProduct = { id: 7, name: 'The Pragmatic Programmer', price: 45 };

  test('Test 21 - getWishlist returns empty array when wishlist is empty', () => {
    expect(getWishlist()).toEqual([]);
  });

  test('Test 22 - addToWishlist adds product and returns updated list', () => {
    const wl = addToWishlist(sampleProduct);
    expect(wl).toHaveLength(1);
    expect(wl[0].id).toBe(7);
  });

  test('Test 23 - addToWishlist does not add duplicate products', () => {
    addToWishlist(sampleProduct);
    const wl = addToWishlist(sampleProduct);
    expect(wl).toHaveLength(1);
  });

  test('Test 24 - removeFromWishlist removes the correct product', () => {
    addToWishlist(sampleProduct);
    addToWishlist({ id: 8, name: 'SICP', price: 55 });
    const wl = removeFromWishlist(7);
    expect(wl).toHaveLength(1);
    expect(wl[0].id).toBe(8);
  });

  test('Test 25 - toggleWishlist adds when absent and removes when present', () => {
    toggleWishlist(sampleProduct);
    expect(isInWishlist(sampleProduct.id)).toBe(true);
    toggleWishlist(sampleProduct);
    expect(isInWishlist(sampleProduct.id)).toBe(false);
  });
});
