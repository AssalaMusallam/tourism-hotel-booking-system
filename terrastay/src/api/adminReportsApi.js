import api from './axios';

export const getRevenueReport = ({ hotelId, from, to }) =>
  api.get('/api/admin/reports/revenue', { params: { hotelId, from, to } }).then((r) => r.data);

export const getOccupancyReport = ({ hotelId, month }) =>
  api.get('/api/admin/reports/occupancy', { params: { hotelId, month } }).then((r) => r.data);

export const getPopularRooms = (hotelId) =>
  api.get('/api/admin/reports/popular-rooms', { params: { hotelId } }).then((r) => r.data);
