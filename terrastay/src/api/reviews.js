import api from './axios';

export const reviewKeys = {
  all: ['reviews'],
  hotel: (hotelId, params = {}) => ['reviews', 'hotel', hotelId, params],
};

export const mapReviewError = (error) => {
  const message =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Unable to submit review';

  if (message.includes('bookingId is required')) return { field: 'bookingId', message };
  if (message.includes('guestEmail must be a valid email')) return { field: 'guestEmail', message: 'guestEmail must be a valid email' };
  if (message.includes('rating must be at least 1') || message.includes('rating must be at most 5')) {
    return { field: 'rating', message };
  }
  if (message.includes('comment cannot exceed 1000 characters')) {
    return { field: 'comment', message: 'comment cannot exceed 1000 characters' };
  }
  if (message.includes('Reviews can only be submitted for COMPLETED bookings')) {
    return { toast: 'Your stay must be completed before leaving a review' };
  }
  if (message.includes('The provided email does not match the booking guest email.')) {
    return { toast: "Email doesn't match the booking" };
  }
  if (message.includes('Cannot submit a review for a booking without a successful payment.')) {
    return { toast: 'A successful payment is required to leave a review' };
  }
  if (message.includes('Review period has expired')) {
    return { toast: 'Review period expired (30 days after checkout)' };
  }
  if (message.includes('A review already exists for booking id:')) {
    return { toast: "You've already reviewed this stay" };
  }

  return { toast: message };
};

export const submitReview = (data) =>
  api.post('/api/reviews', data).then((response) => response.data);

export const getHotelReviews = (hotelId, params = {}) =>
  api.get(`/api/hotels/${hotelId}/reviews`, { params }).then((response) => response.data);
