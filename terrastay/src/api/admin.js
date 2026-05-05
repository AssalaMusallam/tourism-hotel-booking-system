import api from './axios';
import { mockHotels, mockBookings } from './mockData';

const simulateDelay = (ms = 500) => new Promise((r) => setTimeout(r, ms));

export const getAdminStats = async () => {
  try {
    const { data } = await api.get('/admin/stats');
    return data;
  } catch {
    await simulateDelay();
    const totalRevenue = mockBookings
      .filter((b) => b.status === 'CONFIRMED')
      .reduce((sum, b) => sum + b.totalPrice, 0);
    const thisMonth = mockBookings.filter((b) => {
      const d = new Date(b.createdAt);
      const now = new Date();
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    });
    return {
      totalHotels: mockHotels.length,
      totalRooms: mockHotels.reduce((sum, h) => sum + (h.rooms?.length || 0), 0),
      bookingsThisMonth: thisMonth.length,
      revenueThisMonth: thisMonth.filter((b) => b.status === 'CONFIRMED').reduce((sum, b) => sum + b.totalPrice, 0),
      totalRevenue,
      totalBookings: mockBookings.length,
    };
  }
};
