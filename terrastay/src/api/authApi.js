import api from './axios';

// POST /auth/login  → { token, type, user: { id, fullName, email, phone, role, active, createdAt } }
export const login = (data) =>
  api.post('/auth/login', data).then((r) => r.data);

// POST /auth/register  → same AuthResponse shape
export const register = (data) =>
  api.post('/auth/register', data).then((r) => r.data);
