import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  bookingKeys,
  cancelBooking,
  createBooking,
  getBookingById,
  getMyBookings,
} from '../api/bookings';

export const useBooking = (id) =>
  useQuery({
    queryKey: bookingKeys.detail(id),
    queryFn: () => getBookingById(id),
    enabled: Boolean(id),
    staleTime: 2 * 60 * 1000,
  });

export const useMyBookings = (params) =>
  useQuery({
    queryKey: bookingKeys.mine(params),
    queryFn: () => getMyBookings(params),
    staleTime: 60 * 1000,
    placeholderData: (previous) => previous,
  });

export const useCreateBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createBooking,
    onSuccess: (booking) => {
      queryClient.invalidateQueries({ queryKey: bookingKeys.all });
      queryClient.setQueryData(bookingKeys.detail(booking.id), booking);
    },
  });
};

export const useCancelBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }) => cancelBooking(id, reason),
    onSuccess: (booking) => {
      queryClient.invalidateQueries({ queryKey: bookingKeys.all });
      queryClient.setQueryData(bookingKeys.detail(booking.id), booking);
    },
  });
};
