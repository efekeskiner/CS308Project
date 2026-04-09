const BASE_URL = "http://localhost:8080/api/auth";

export async function login(email, password) {
    const res = await fetch(`${BASE_URL}/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
    });
    if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.message || "Invalid credentials");
    }
    const data = await res.json();
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);
    localStorage.setItem("user", JSON.stringify(data.user));
    return data;
}

export async function register(name, email, phone, password) {
    const res = await fetch(`${BASE_URL}/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, email, phone, password }),
    });
    if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.message || "Registration failed");
    }
    return res.json();
}

export function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
}

export function getToken() {
    return localStorage.getItem("accessToken");
}

export function getCurrentUser() {
    const u = localStorage.getItem("user");
    return u ? JSON.parse(u) : null;
}

export function isLoggedIn() {
    return !!getToken();
}

/**
 * Korumalı API cagrıları icin Authorization header'ını otomatik ekler.
 * Kullanim: authFetch("/api/some-endpoint", { method: "GET" })
 */
export function authFetch(url, options = {}) {
    const token = getToken();
    return fetch(url, {
          ...options,
          headers: {
                  "Content-Type": "application/json",
                  ...(options.headers || {}),
                  ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
    });
}
