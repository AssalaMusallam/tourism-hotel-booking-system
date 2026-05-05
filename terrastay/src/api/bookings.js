import api from './axios';
import { mockBookings } from './mockData';

const simulateDelay = (ms = 600) => new Promise((r) => setTimeout(r, ms));

export const createBooking = async (bookingData) => {
  try {
    const { data } = await api.post('/bookings', bookingData);
    return data;
  } catch {
    await simulateDelay(1000);
    const ref = 'BK' + String(Date.now()).slice(-6);
    return {
      id: ref,
      ...bookingData,
      status: 'CONFIRMED',
      createdAt: new Date().toISOString(),
    };
  }
};

export const getMyBookings = async () => {
  try {
    const { data } = await api.get('/bookings/my');
    return data;
  } catch {
    await simulateDelay();
    const user = JSON.parse(localStorage.getItem('terrastay_user') || '{}');
    return mockBookings.filter((b) => !user.email || b.guestEmail === user.email).slice(0, 5);
  }
};

export const getBookingById = async (id) => {
  try {
    const { data } = await api.get(`/bookings/${id}`);
    return data;
  } catch {
    await simulateDelay(300);
    const booking = mockBookings.find((b) => b.id === id);
    if (!booking) throw new Error('Booking not found');
    return booking;
  }
};

export const cancelBooking = async (id) => {
  try {
    await api.delete(`/bookings/${id}`);
  } catch {
    await simulateDelay();
  }
};

export const getAllBookings = async (params = {}) => {
  try {
    const { data } = await api.get('/admin/bookings', { params });
    return data;
  } catch {
    await simulateDelay();
    let results = [...mockBookings];
    if (params.status) results = results.filter((b) => b.status === params.status);
    if (params.city) results = results.filter((b) => b.city === params.city);
    return { data: results, total: results.length };
  }
};
