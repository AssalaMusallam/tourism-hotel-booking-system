import api from './axios';

// All amenity write operations require ADMIN role.
// All amenity GET operations are PUBLIC.
//
// AmenityResponseDTO: { id, name, description, category, premium, active, createdAt, updatedAt }
// AmenityMinimalDTO:  { id, name }
//
// sort format for GET /api/amenities: "field,asc" or "field,desc"
// Allowed sort fields: id, name, category, createdAt, updatedAt

export const getAmenities = (params) =>
  api.get('/api/amenities', { params }).then((r) => r.data);

export const getAmenityById = (id) =>
  api.get(`/api/amenities/${id}`).then((r) => r.data);

export const checkAmenityExists = (name) =>
  api.get('/api/amenities/exists', { params: { name } }).then((r) => r.data);

// Returns PagedResponse<AmenityMinimalDTO>
export const getMinimalAmenities = (params) =>
  api.get('/api/amenities/minimal', { params }).then((r) => r.data);

// Returns List<AmenityMinimalDTO>
export const suggestAmenities = (q, active) =>
  api.get('/api/amenities/suggest', { params: { q, active } }).then((r) => r.data);

// ─── Write (ADMIN only) ───────────────────────────────────────────────────────
// body: { name(3-100)*, description(10-500)*, category*, premium?, active? }
export const createAmenity = (data) =>
  api.post('/api/amenities', data).then((r) => r.data);

export const updateAmenity = (id, data) =>
  api.put(`/api/amenities/${id}`, data).then((r) => r.data);

// Soft delete (sets active=false)
export const deleteAmenity = (id) =>
  api.delete(`/api/amenities/${id}`);

// Hard delete
export const hardDeleteAmenity = (id) =>
  api.delete(`/api/amenities/${id}/hard`);

export const activateAmenity = (id) =>
  api.patch(`/api/amenities/${id}/activate`).then((r) => r.data);

export const deactivateAmenity = (id) =>
  api.patch(`/api/amenities/${id}/deactivate`).then((r) => r.data);

export const restoreAmenity = (id) =>
  api.patch(`/api/amenities/${id}/restore`).then((r) => r.data);

// body: { ids: Long[], active: boolean }
export const bulkAmenityStatus = (ids, active) =>
  api.patch('/api/amenities/status', { ids, active }).then((r) => r.data);
