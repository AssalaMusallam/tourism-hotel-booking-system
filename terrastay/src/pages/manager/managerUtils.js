import api from '../../api/axios';

export const getManagedHotelId = async (user) => {
  if (user?.managedHotelId || user?.hotelId) return user.managedHotelId || user.hotelId;
  try {
    const response = await api.get('/api/manager/my-hotel');
    return response.data?.id || response.data?.hotelId || response.data?.managedHotelId;
  } catch {
    try {
      const response = await api.get('/api/users/me');
      return response.data?.managedHotelId || response.data?.hotelId;
    } catch {
      return null;
    }
  }
};

export const normalizePage = (data, fallback = []) => {
  if (Array.isArray(data)) return { items: data, totalPages: 1, isMock: false };
  return {
    items: data?.content || data?.data || data?.items || fallback,
    totalPages: data?.totalPages || 1,
    isMock: false,
  };
};

export const formatDate = (value) => value ? new Date(value).toLocaleDateString('en') : '-';
export const money = (value) => `$${Number(value || 0).toLocaleString('en')}`;

export const withMock = (items) => ({ items, totalPages: 1, isMock: true });
