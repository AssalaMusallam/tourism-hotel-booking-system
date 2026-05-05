import api from './axios';

// ─── Public ──────────────────────────────────────────────────────────────────
// RoomTypeResponseDto: { id, hotelId, name, capacity, bedType(KING|QUEEN|TWIN),
//   bedCount, maxAdults, maxChildren, basePrice(BigDecimal), totalUnits,
//   description, policies, status(ACTIVE|INACTIVE), amenityIds: Set<Long> }
export const getRoomTypeById = (id) =>
  api.get(`/api/room-types/${id}`).then((r) => r.data);

// params: hotelId, name, bedType, bedCountMin/Max, capacityMin/Max,
//   maxAdultsMin/Max, maxChildrenMin/Max, priceMin/Max, q,
//   page(0-indexed), size, sort(field), dir(asc|desc)
export const searchRoomTypes = (params) =>
  api.get('/api/room-types', { params }).then((r) => r.data);

// GET /api/hotels/{hotelId}/room-types
// Additional params: status(ACTIVE|INACTIVE)
export const getRoomTypesByHotel = (hotelId, params) =>
  api.get(`/api/hotels/${hotelId}/room-types`, { params }).then((r) => r.data);

// GET /api/hotels/{hotelId}/room-types/meta/minimal
// Returns PagedResponse<{ id, name }>
export const getMinimalRoomTypes = (hotelId, params) =>
  api.get(`/api/hotels/${hotelId}/room-types/meta/minimal`, { params }).then((r) => r.data);

export const suggestRoomTypes = (hotelId, q) =>
  api.get(`/api/hotels/${hotelId}/room-types/suggest`, { params: { q } }).then((r) => r.data);

// Needs MANAGER/ADMIN auth
export const adminGetRoomType = (hotelId, id) =>
  api.get(`/api/hotels/${hotelId}/room-types/${id}`).then((r) => r.data);

// ─── Write (MANAGER/ADMIN) ────────────────────────────────────────────────────
// RoomTypeRequestDto: { hotelId*, name*, capacity*, bedType*, bedCount*,
//   maxAdults*, maxChildren*, basePrice*, totalUnits*, description?, policies?,
//   status?, amenityIds? }
export const createRoomType = (hotelId, data) =>
  api.post(`/api/hotels/${hotelId}/room-types`, data).then((r) => r.data);

export const updateRoomType = (hotelId, id, data) =>
  api.put(`/api/hotels/${hotelId}/room-types/${id}`, data).then((r) => r.data);

export const deleteRoomType = (hotelId, id) =>
  api.delete(`/api/hotels/${hotelId}/room-types/${id}`);

// body: { status: "ACTIVE"|"INACTIVE" }
export const changeRoomTypeStatus = (hotelId, id, status) =>
  api.patch(`/api/hotels/${hotelId}/room-types/${id}/status`, { status }).then((r) => r.data);

// body: { ids: Long[], status }
export const bulkChangeRoomTypeStatus = (hotelId, ids, status) =>
  api.patch(`/api/hotels/${hotelId}/room-types/bulk-status`, { ids, status }).then((r) => r.data);

// ─── Amenities on Room Types (ADMIN/MANAGER) ──────────────────────────────────
// body: { amenityIds: Long[] }
export const replaceRoomAmenities = (roomTypeId, amenityIds) =>
  api.put(`/api/admin/room-types/${roomTypeId}/amenities`, { amenityIds }).then((r) => r.data);

export const addAmenityToRoom = (roomTypeId, amenityId) =>
  api.patch(`/api/admin/room-types/${roomTypeId}/amenities/${amenityId}`).then((r) => r.data);

export const removeAmenityFromRoom = (roomTypeId, amenityId) =>
  api.delete(`/api/admin/room-types/${roomTypeId}/amenities/${amenityId}`).then((r) => r.data);
