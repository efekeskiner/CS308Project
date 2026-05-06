import { authFetch } from "./auth";

const API_URL = "http://localhost:8080/api";

export function getInvoicePdfUrl(invoiceId) {
  return `${API_URL}/invoices/${invoiceId}/pdf`;
}

export async function downloadInvoicePdf(invoiceId, orderId) {
  const res = await authFetch(`${API_URL}/invoices/${invoiceId}/pdf`);
  if (!res.ok) throw new Error("Could not download invoice PDF");
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `invoice-${orderId || invoiceId}.pdf`;
  a.click();
  URL.revokeObjectURL(url);
}
