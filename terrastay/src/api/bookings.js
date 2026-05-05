import api from './axios';

export const bookingKeys = {
  all: ['bookings'],
  mine: (params = {}) => ['bookings', 'mine', params],
  detail: (id) => ['bookings', 'detail', id],
};

export const friendlyBookingError = (error) => {
  const message =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Something went wrong';

  if (message.includes('roomTypeId is required')) return 'Please select a room';
  if (message.includes('Requested') && message.includes('guests exceeds capacity')) return message;
  if (message.includes('checkOut must be a future date')) return 'Check-out date must be in the future';
  if (message.includes('is not available from')) return 'Room not available for these dates';
  if (message.includes('Cannot transition booking')) return 'Action not allowed for this booking';
  if (message.includes('Cancellation reason is required')) return 'Please enter a cancellation reason';
  if (message.includes('Booking with id') && message.includes('was not found')) return 'Booking not found';
  return message;
};

export const createBooking = (bookingData) =>
  api.post('/api/bookings', bookingData).then((response) => response.data);

export const getMyBookings = (params = {}) =>
  api.get('/api/bookings/my', { params }).then((response) => response.data);

export const getBookingById = (id) =>
  api.get(`/api/bookings/${id}`).then((response) => response.data);

export const cancelBooking = (id, reason) =>
  api.patch(`/api/bookings/${id}/cancel`, { reason }).then((response) => response.data);

export const getAllBookings = (params = {}) =>
  api.get('/api/bookings', { params }).then((response) => response.data);

export const confirmBooking = (id) =>
  api.patch(`/api/bookings/${id}/confirm`).then((response) => response.data);

export const completeBooking = (id) =>
  api.patch(`/api/bookings/${id}/complete`).then((response) => response.data);
