import api from './axios';

export const getMe = () => api.get('/api/users/me').then((response) => response.data);

export const updateMe = (data) => api.put('/api/users/me', data).then((response) => response.data);

export const getUsers = (params = {}) =>
  api.get('/api/admin/users', { params }).then((response) => response.data);

export const updateUserStatus = (id, active) =>
  api.patch(`/api/admin/users/${id}/status`, { active }).then((response) => response.data);

export const updateUserRole = (id, role) =>
  api.patch(`/api/admin/users/${id}/role`, { role }).then((response) => response.data);

export const getManagedHotelsForUser = (id) =>
  api.get(`/api/admin/users/${id}/hotels`).then((response) => response.data);
