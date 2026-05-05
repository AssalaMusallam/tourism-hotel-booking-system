import api from './axios';

export const paymentKeys = {
  all: ['payments'],
  detail: (id) => ['payments', 'detail', id],
  latestByBooking: (bookingId) => ['payments', 'booking', bookingId, 'latest'],
  history: (bookingId) => ['payments', 'booking', bookingId, 'history'],
};

export const friendlyPaymentError = (error) => {
  const message =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Payment failed';

  if (message.includes('bookingId is required')) return 'Booking ID is missing';
  if (message.includes('amount must be greater than 0')) return 'Invalid payment amount';
  if (message.includes('currency must be a 3-letter ISO code')) return 'Invalid currency format';
  if (error?.response?.status === 409) return message;
  if (error?.response?.status === 404) return 'Payment not found';
  return message;
};

export const createPaymentIntent = ({ bookingId, amount, currency = 'USD', method = 'MOCK_CARD' }) =>
  api.post('/api/payments/intents', { bookingId, amount, currency, method }).then((response) => response.data);

export const simulatePaymentSuccess = (id) =>
  api.post(`/api/payments/${id}/simulate-success`).then((response) => response.data);

export const simulatePaymentFailure = (id, reason = 'Insufficient funds') =>
  api.post(`/api/payments/${id}/simulate-failure`, { reason }).then((response) => response.data);

export const getPaymentById = (id) =>
  api.get(`/api/payments/${id}`).then((response) => response.data);

export const getPaymentHistory = (bookingId) =>
  api.get(`/api/payments/booking/${bookingId}/history`).then((response) => response.data);

export const getLatestPayment = (bookingId) =>
  api.get(`/api/payments/booking/${bookingId}`).then((response) => response.data);

export const refundPayment = (id, reason) =>
  api.post(`/api/payments/${id}/refund`, reason ? { reason } : {}).then((response) => response.data);
