import { useQuery } from '@tanstack/react-query';
import { getRevenueReport, getOccupancyReport, getPopularRooms } from '../api/adminReportsApi';

/**
 * Revenue report for a hotel within a date range.
 * Returns RevenueReportDto.
 */
export const useRevenueReport = (hotelId, from, to, enabled = true) =>
  useQuery({
    queryKey: ['admin', 'reports', 'revenue', hotelId, from, to],
    queryFn: () => getRevenueReport({ hotelId, from, to }),
    enabled: !!(hotelId && from && to && enabled),
    staleTime: 5 * 60 * 1000,
    retry: false,
  });

/**
 * Occupancy rate for a hotel in a given month (YYYY-MM).
 * Returns OccupancyReportDto.
 */
export const useOccupancyReport = (hotelId, month, enabled = true) =>
  useQuery({
    queryKey: ['admin', 'reports', 'occupancy', hotelId, month],
    queryFn: () => getOccupancyReport({ hotelId, month }),
    enabled: !!(hotelId && month && enabled),
    staleTime: 5 * 60 * 1000,
    retry: false,
  });

/**
 * Most popular room types by booking count for a hotel.
 * Returns PopularRoomDto[].
 */
export const usePopularRooms = (hotelId, enabled = true) =>
  useQuery({
    queryKey: ['admin', 'reports', 'popular-rooms', hotelId],
    queryFn: () => getPopularRooms(hotelId),
    enabled: !!(hotelId && enabled),
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
