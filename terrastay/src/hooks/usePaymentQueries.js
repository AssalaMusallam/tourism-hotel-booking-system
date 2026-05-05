import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createPaymentIntent,
  getLatestPayment,
  getPaymentById,
  getPaymentHistory,
  paymentKeys,
  refundPayment,
  simulatePaymentFailure,
  simulatePaymentSuccess,
} from '../api/payments';

export const usePayment = (id) =>
  useQuery({
    queryKey: paymentKeys.detail(id),
    queryFn: () => getPaymentById(id),
    enabled: Boolean(id),
    staleTime: 60 * 1000,
  });

export const usePaymentHistory = (bookingId) =>
  useQuery({
    queryKey: paymentKeys.history(bookingId),
    queryFn: () => getPaymentHistory(bookingId),
    enabled: Boolean(bookingId),
    staleTime: 60 * 1000,
  });

export const useLatestPayment = (bookingId) =>
  useQuery({
    queryKey: paymentKeys.latestByBooking(bookingId),
    queryFn: () => getLatestPayment(bookingId),
    enabled: Boolean(bookingId),
    retry: false,
    staleTime: 60 * 1000,
  });

export const usePayNow = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ bookingId, amount }) => {
      const intent = await createPaymentIntent({ bookingId, amount, currency: 'USD', method: 'MOCK_CARD' });
      return simulatePaymentSuccess(intent.id);
    },
    onSuccess: (payment) => {
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
      queryClient.setQueryData(paymentKeys.detail(payment.id), payment);
    },
  });
};

export const useSimulatePaymentFailure = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ bookingId, amount, reason = 'Insufficient funds' }) => {
      const intent = await createPaymentIntent({ bookingId, amount, currency: 'USD', method: 'MOCK_CARD' });
      return simulatePaymentFailure(intent.id, reason);
    },
    onSuccess: (payment) => {
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
      queryClient.setQueryData(paymentKeys.detail(payment.id), payment);
    },
  });
};

export const useRefundPayment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }) => refundPayment(id, reason),
    onSuccess: (payment) => {
      queryClient.invalidateQueries({ queryKey: paymentKeys.all });
      queryClient.setQueryData(paymentKeys.detail(payment.id), payment);
    },
  });
};
