const API_URL = "http://localhost:8080/api";

export function getInvoicePdfUrl(invoiceId) {
  return `${API_URL}/invoices/${invoiceId}/pdf`;
}
