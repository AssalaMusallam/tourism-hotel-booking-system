import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';

const staleTime = 60000;

const getOrFallback = async (url, fallback, config) => {
  try {
    const response = await api.get(url, config);
    return response.data;
  } catch {
    return fallback;
  }
};

export const useAdminSummary = () =>
  useQuery({
    queryKey: ['admin', 'dashboard', 'summary'],
    queryFn: () => getOrFallback('/api/admin/reports/summary', {
      monthlyBookings: 0,
      totalRevenue: 0,
      occupancyRate: 0,
      pendingBookings: 0,
    }),
    staleTime,
  });

export const useAdminRevenue = () =>
  useQuery({
    queryKey: ['admin', 'dashboard', 'revenue-monthly'],
    queryFn: () => getOrFallback('/api/admin/reports/revenue-monthly', []),
    staleTime,
  });

export const useAdminBookingStatus = () =>
  useQuery({
    queryKey: ['admin', 'dashboard', 'booking-status'],
    queryFn: () => getOrFallback('/api/admin/reports/booking-status', []),
    staleTime,
  });

export const useAdminRecentBookings = () =>
  useQuery({
    queryKey: ['admin', 'dashboard', 'recent-bookings'],
    queryFn: () => getOrFallback('/api/bookings', { content: [] }, {
      params: { size: 10, sort: 'createdAt,desc' },
    }),
    staleTime,
  });

export const useAdminPopularRooms = () =>
  useQuery({
    queryKey: ['admin', 'dashboard', 'popular-rooms'],
    queryFn: () => getOrFallback('/api/admin/reports/popular-rooms', []),
    staleTime,
  });
