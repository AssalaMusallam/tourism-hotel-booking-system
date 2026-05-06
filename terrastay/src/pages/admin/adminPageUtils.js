export const normalizePage = (data, fallbackItems = []) => {
  if (Array.isArray(data)) {
    return { items: data, totalPages: 1, totalElements: data.length };
  }

  const items = data?.content || data?.data || data?.items || fallbackItems;
  return {
    items: Array.isArray(items) ? items : fallbackItems,
    totalPages: data?.totalPages || data?.pageCount || 1,
    totalElements: data?.totalElements || data?.total || items?.length || 0,
  };
};

export const formatDate = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleDateString('en', { year: 'numeric', month: 'short', day: 'numeric' });
};

export const formatMoney = (value) => `$${Number(value || 0).toLocaleString('en', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
})}`;

export const getGuestName = (row) =>
  row.guestName || row.guest?.fullName || row.user?.fullName || row.customerName || row.name || '-';

export const getHotelName = (row) =>
  row.hotelName || row.hotel?.nameEn || row.hotel?.name || row.booking?.hotelName || '-';

export const getRoomName = (row) =>
  row.roomTypeName || row.roomType?.nameEn || row.roomType?.name || row.room?.name || row.booking?.roomTypeName || '-';
