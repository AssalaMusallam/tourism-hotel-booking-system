import api from './axios';

// ─── Public ──────────────────────────────────────────────────────────────────
// GET /api/hotels  params: q, city, country, amenity, minRating, maxRating,
//   hasImage, hasPhone, hasWebsite, hasEmail, page(0-indexed), size
// Returns: PagedResponse<HotelResponseDto>
//   { content, totalElements, totalPages, number, size, first, last }
// HotelResponseDto: { id, name, address, description,
//   images:[{id, imageUrl, fileName}], phoneNumber, email, websiteUrl,
//   rating, city, country, latitude, longitude,
//   checkInTime, checkOutTime, policies, cancellationPolicySummary,
//   status("ACTIVE"|"INACTIVE"), amenityNames: Set<String> }
export const searchHotels = (params) =>
  api.get('/api/hotels', { params }).then((r) => r.data);

export const getHotelById = (id) =>
  api.get(`/api/hotels/${id}`).then((r) => r.data);

export const getCities = () =>
  api.get('/api/hotels/cities').then((r) => r.data);

export const getCountries = () =>
  api.get('/api/hotels/countries').then((r) => r.data);

export const autocompleteHotels = (q, limit = 10) =>
  api.get('/api/hotels/autocomplete', { params: { q, limit } }).then((r) => r.data);

// ─── Admin / Manager ─────────────────────────────────────────────────────────
export const adminSearchHotels = (params) =>
  api.get('/api/admin/hotels', { params }).then((r) => r.data);

export const adminGetHotelById = (id) =>
  api.get(`/api/admin/hotels/${id}`).then((r) => r.data);

export const createHotel = (data) =>
  api.post('/api/admin/hotels', data).then((r) => r.data);

export const updateHotel = (id, data) =>
  api.put(`/api/admin/hotels/${id}`, data).then((r) => r.data);

export const patchHotel = (id, data) =>
  api.patch(`/api/admin/hotels/${id}`, data).then((r) => r.data);

export const deleteHotel = (id) =>
  api.delete(`/api/admin/hotels/${id}`);

// Images  – FormData field: "files"
export const uploadHotelImages = (hotelId, files) => {
  const form = new FormData();
  files.forEach((f) => form.append('files', f));
  return api.post(`/api/admin/hotels/${hotelId}/images`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then((r) => r.data);
};

export const getHotelImages = (hotelId) =>
  api.get(`/api/admin/hotels/${hotelId}/images`).then((r) => r.data);

export const deleteHotelImage = (hotelId, imageId) =>
  api.delete(`/api/admin/hotels/${hotelId}/images/${imageId}`);

// Managers (ADMIN only)
export const getHotelManagers = (hotelId) =>
  api.get(`/api/admin/hotels/${hotelId}/managers`).then((r) => r.data);

export const assignManager = (hotelId, userId) =>
  api.post(`/api/admin/hotels/${hotelId}/managers/${userId}`);

export const removeManager = (hotelId, userId) =>
  api.delete(`/api/admin/hotels/${hotelId}/managers/${userId}`);
