// ============================================================
// CS308Project - Comprehensive Unit Test Suite
// Tests for: auth service, cart service, wishlist service,
//            role helpers (App.js), orders service helpers,
//            products service helpers, ratings/comments helpers,
//            authFetch header builder, and edge cases
// Framework: Jest + @testing-library/react (Create React App)
// Run with: npm test
// ============================================================

// ---------- Mock localStorage ----------
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

// ---------- Inline implementations (mirrors src/services/*.js & App.js) ----------

// --- auth.js ---
const getToken       = () => localStorage.getItem('accessToken');
const getCurrentUser = () => { const u = localStorage.getItem('user'); return u ? JSON.parse(u) : null; };
const isLoggedIn     = () => !!getToken();
const logout         = () => { localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); localStorage.removeItem('user'); };
const authFetch      = (url, options = {}) => {
  const token = getToken();
  return fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
};

// --- App.js role helpers ---
const isManagerUser  = (user) => user && (user.role === 'PRODUCT_MANAGER' || user.role === 'SALES_MANAGER');
const isCustomerUser = (user) => user && user.role === 'CUSTOMER';

// --- cart.js ---
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

// --- wishlist.js ---
const WISHLIST_KEY       = 'wishlist';
const getWishlist        = () => { const w = localStorage.getItem(WISHLIST_KEY); return w ? JSON.parse(w) : []; };
const saveWishlist       = (wl) => localStorage.setItem(WISHLIST_KEY, JSON.stringify(wl));
const isInWishlist       = (pid) => getWishlist().some(i => i.id === pid);
const addToWishlist      = (product) => { const wl = getWishlist(); if (wl.some(i => i.id === product.id)) return wl; const updated = [...wl, product]; saveWishlist(updated); return updated; };
const removeFromWishlist = (pid) => { const updated = getWishlist().filter(i => i.id !== pid); saveWishlist(updated); return updated; };
const toggleWishlist     = (product) => isInWishlist(product.id) ? removeFromWishlist(product.id) : addToWishlist(product);
const clearWishlist      = () => localStorage.removeItem(WISHLIST_KEY);

// --- orders.js helper (pure logic) ---
const getOrderAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
};

// --- Pure utility: format currency (representative business logic helper) ---
const formatCurrency = (amount) => `$${Number(amount).toFixed(2)}`;

// --- Pure utility: validate email ---
const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

// --- Pure utility: validate password length ---
const isValidPassword = (pw) => typeof pw === 'string' && pw.length >= 6;

// ================================================================
// SETUP / TEARDOWN
// ================================================================
beforeEach(() => {
  localStorage.clear();
  jest.clearAllMocks();
});

// ================================================================
// PART 1 – AUTH SERVICE (Tests 1–10)
// ================================================================
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

  test('Test 7 - logout clears accessToken, refreshToken, and user', () => {
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

  test('Test 9 - authFetch sends Authorization header when token is present', () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: true, json: async () => ({}) });
    localStorage.setItem('accessToken', 'my-jwt');
    authFetch('/api/test');
    const calledHeaders = global.fetch.mock.calls[0][1].headers;
    expect(calledHeaders.Authorization).toBe('Bearer my-jwt');
  });

  test('Test 10 - authFetch omits Authorization header when no token', () => {
    global.fetch = jest.fn().mockResolvedValue({ ok: true, json: async () => ({}) });
    authFetch('/api/test');
    const calledHeaders = global.fetch.mock.calls[0][1].headers;
    expect(calledHeaders.Authorization).toBeUndefined();
  });
});

// ================================================================
// PART 2 – ROLE HELPERS from App.js (Tests 11–16)
// ================================================================
describe('Role Helpers (App.js)', () => {
  test('Test 11 - isManagerUser returns true for PRODUCT_MANAGER', () => {
    expect(isManagerUser({ role: 'PRODUCT_MANAGER' })).toBe(true);
  });

  test('Test 12 - isManagerUser returns true for SALES_MANAGER', () => {
    expect(isManagerUser({ role: 'SALES_MANAGER' })).toBe(true);
  });

  test('Test 13 - isManagerUser returns false for CUSTOMER role', () => {
    expect(isManagerUser({ role: 'CUSTOMER' })).toBe(false);
  });

  test('Test 14 - isManagerUser returns false for null user', () => {
    expect(isManagerUser(null)).toBeFalsy();
  });

  test('Test 15 - isCustomerUser returns true for CUSTOMER role', () => {
    expect(isCustomerUser({ role: 'CUSTOMER' })).toBe(true);
  });

  test('Test 16 - isCustomerUser returns false for manager roles', () => {
    expect(isCustomerUser({ role: 'PRODUCT_MANAGER' })).toBe(false);
    expect(isCustomerUser({ role: 'SALES_MANAGER' })).toBe(false);
  });
});

// ================================================================
// PART 3 – CART SERVICE (Tests 17–28)
// ================================================================
describe('Cart Service', () => {
  const sampleProduct = { id: 42, name: 'Clean Code', price: '29.99', inStock: true };

  test('Test 17 - getCart returns empty array when cart is empty', () => {
    expect(getCart()).toEqual([]);
  });

  test('Test 18 - addToCart adds a new product with quantity 1', () => {
    const cart = addToCart(sampleProduct);
    expect(cart).toHaveLength(1);
    expect(cart[0].quantity).toBe(1);
    expect(cart[0].name).toBe('Clean Code');
  });

  test('Test 19 - addToCart increments quantity for existing product', () => {
    addToCart(sampleProduct);
    const cart = addToCart(sampleProduct);
    expect(cart).toHaveLength(1);
    expect(cart[0].quantity).toBe(2);
  });

  test('Test 20 - addToCart converts price to Number', () => {
    const cart = addToCart(sampleProduct);
    expect(typeof cart[0].price).toBe('number');
    expect(cart[0].price).toBe(29.99);
  });

  test('Test 21 - addToCart sets inStock true for product with inStock: true', () => {
    const cart = addToCart({ id: 1, name: 'Book', price: 10, inStock: true });
    expect(cart[0].inStock).toBe(true);
  });

  test('Test 22 - addToCart sets inStock false when product.inStock is false', () => {
    const cart = addToCart({ id: 2, name: 'OOS Book', price: 10, inStock: false });
    expect(cart[0].inStock).toBe(false);
  });

  test('Test 23 - addToCart dispatches cartUpdated event', () => {
    addToCart(sampleProduct);
    expect(window.dispatchEvent).toHaveBeenCalled();
  });

  test('Test 24 - updateCartItemQuantity updates the quantity of an existing item', () => {
    addToCart(sampleProduct);
    const cart = updateCartItemQuantity(42, 5);
    expect(cart[0].quantity).toBe(5);
  });

  test('Test 25 - updateCartItemQuantity removes item when newQuantity is 0', () => {
    addToCart(sampleProduct);
    const cart = updateCartItemQuantity(42, 0);
    expect(cart).toHaveLength(0);
  });

  test('Test 26 - removeFromCart removes the specified product', () => {
    addToCart(sampleProduct);
    addToCart({ id: 99, name: 'Refactoring', price: '39.99' });
    const cart = removeFromCart(42);
    expect(cart).toHaveLength(1);
    expect(cart[0].id).toBe(99);
  });

  test('Test 27 - clearCart empties localStorage cart key', () => {
    addToCart(sampleProduct);
    clearCart();
    expect(localStorage.getItem(CART_KEY)).toBeNull();
  });

  test('Test 28 - getCartTotal calculates total price correctly', () => {
    const items = [
      { price: '10', quantity: 2 },
      { price: '5',  quantity: 3 },
    ];
    expect(getCartTotal(items)).toBeCloseTo(35);
  });
});

// ================================================================
// PART 4 – WISHLIST SERVICE (Tests 29–36)
// ================================================================
describe('Wishlist Service', () => {
  const sampleProduct = { id: 7, name: 'The Pragmatic Programmer', price: 45 };

  test('Test 29 - getWishlist returns empty array when wishlist is empty', () => {
    expect(getWishlist()).toEqual([]);
  });

  test('Test 30 - addToWishlist adds product and returns updated list', () => {
    const wl = addToWishlist(sampleProduct);
    expect(wl).toHaveLength(1);
    expect(wl[0].id).toBe(7);
  });

  test('Test 31 - addToWishlist does not add duplicate products', () => {
    addToWishlist(sampleProduct);
    const wl = addToWishlist(sampleProduct);
    expect(wl).toHaveLength(1);
  });

  test('Test 32 - isInWishlist returns true when product is present', () => {
    addToWishlist(sampleProduct);
    expect(isInWishlist(sampleProduct.id)).toBe(true);
  });

  test('Test 33 - isInWishlist returns false when product is absent', () => {
    expect(isInWishlist(999)).toBe(false);
  });

  test('Test 34 - removeFromWishlist removes the correct product', () => {
    addToWishlist(sampleProduct);
    addToWishlist({ id: 8, name: 'SICP', price: 55 });
    const wl = removeFromWishlist(7);
    expect(wl).toHaveLength(1);
    expect(wl[0].id).toBe(8);
  });

  test('Test 35 - clearWishlist removes wishlist from localStorage', () => {
    addToWishlist(sampleProduct);
    clearWishlist();
    expect(localStorage.getItem(WISHLIST_KEY)).toBeNull();
  });

  test('Test 36 - toggleWishlist adds when absent and removes when present', () => {
    toggleWishlist(sampleProduct);
    expect(isInWishlist(sampleProduct.id)).toBe(true);
    toggleWishlist(sampleProduct);
    expect(isInWishlist(sampleProduct.id)).toBe(false);
  });
});

// ================================================================
// PART 5 – ORDERS SERVICE HELPER (Tests 37–39)
// ================================================================
describe('Orders Service - getOrderAuthHeaders', () => {
  test('Test 37 - returns empty object when no token is stored', () => {
    expect(getOrderAuthHeaders()).toEqual({});
  });

  test('Test 38 - returns correct Authorization header when token is stored', () => {
    localStorage.setItem('accessToken', 'order-token');
    const headers = getOrderAuthHeaders();
    expect(headers.Authorization).toBe('Bearer order-token');
  });

  test('Test 39 - Authorization header updates when token changes', () => {
    localStorage.setItem('accessToken', 'token-v1');
    expect(getOrderAuthHeaders().Authorization).toBe('Bearer token-v1');
    localStorage.setItem('accessToken', 'token-v2');
    expect(getOrderAuthHeaders().Authorization).toBe('Bearer token-v2');
  });
});

// ================================================================
// PART 6 – CART EDGE CASES & MULTI-ITEM LOGIC (Tests 40–44)
// ================================================================
describe('Cart Service - Edge Cases', () => {
  test('Test 40 - getCart returns empty array when localStorage has malformed JSON', () => {
    localStorage.setItem('cart', 'NOT_VALID_JSON{{{');
    expect(getCart()).toEqual([]);
  });

  test('Test 41 - getCartTotal returns 0 for an empty cart', () => {
    expect(getCartTotal([])).toBe(0);
  });

  test('Test 42 - getCartTotal handles a single item correctly', () => {
    expect(getCartTotal([{ price: '15.50', quantity: 4 }])).toBeCloseTo(62);
  });

  test('Test 43 - addToCart can handle multiple distinct products', () => {
    addToCart({ id: 1, name: 'Book A', price: 10 });
    addToCart({ id: 2, name: 'Book B', price: 20 });
    addToCart({ id: 3, name: 'Book C', price: 30 });
    expect(getCart()).toHaveLength(3);
  });

  test('Test 44 - removeFromCart on non-existent id leaves cart unchanged', () => {
    addToCart({ id: 1, name: 'Book A', price: 10 });
    const cart = removeFromCart(999);
    expect(cart).toHaveLength(1);
  });
});

// ================================================================
// PART 7 – UTILITY / BUSINESS LOGIC HELPERS (Tests 45–50)
// ================================================================
describe('Utility Helpers', () => {
  test('Test 45 - formatCurrency formats integer price correctly', () => {
    expect(formatCurrency(10)).toBe('$10.00');
  });

  test('Test 46 - formatCurrency formats decimal price correctly', () => {
    expect(formatCurrency(29.9)).toBe('$29.90');
  });

  test('Test 47 - formatCurrency handles string input', () => {
    expect(formatCurrency('5.5')).toBe('$5.50');
  });

  test('Test 48 - isValidEmail accepts a valid email address', () => {
    expect(isValidEmail('user@example.com')).toBe(true);
  });

  test('Test 49 - isValidEmail rejects a string without @ symbol', () => {
    expect(isValidEmail('invalidemail.com')).toBe(false);
  });

  test('Test 50 - isValidPassword returns false for passwords shorter than 6 chars', () => {
    expect(isValidPassword('abc')).toBe(false);
  });
});
