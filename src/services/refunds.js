import { authFetch } from "./auth";

const BASE_URL = "http://localhost:8080/api/refunds";

async function parseResponse(res, fallbackMessage) {
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    let message = fallbackMessage;

    try {
      const json = text ? JSON.parse(text) : {};
      message = json.message || json.error || fallbackMessage;
    } catch {
      message = text || fallbackMessage;
    }

    throw new Error(message);
  }

  if (res.status === 204) return null;
  return res.json();
}

export async function requestRefund(orderItemId, reason = "") {
  const res = await authFetch(BASE_URL, {
    method: "POST",
    body: JSON.stringify({
      orderItemId,
      reason: reason?.trim() || null,
    }),
  });

  return parseResponse(res, "Could not request refund.");
}

export async function getMyRefunds() {
  const res = await authFetch(`${BASE_URL}/mine`);
  return parseResponse(res, "Could not load your refund requests.");
}

export async function listPendingRefunds() {
  const res = await authFetch(BASE_URL);
  return parseResponse(res, "Could not load refund requests.");
}

export async function approveRefund(id) {
  const res = await authFetch(`${BASE_URL}/${id}/approve`, {
    method: "PUT",
  });

  return parseResponse(res, "Could not approve refund.");
}

export async function rejectRefund(id) {
  const res = await authFetch(`${BASE_URL}/${id}/reject`, {
    method: "PUT",
  });

  return parseResponse(res, "Could not reject refund.");
}