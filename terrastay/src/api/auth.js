import api from './axios';
import { MOCK_USER, MOCK_ADMIN } from './mockData';

const simulateDelay = (ms = 600) => new Promise((r) => setTimeout(r, ms));

export const login = async ({ email, password }) => {
  try {
    const { data } = await api.post('/auth/login', { email, password });
    return data;
  } catch {
    await simulateDelay();
    if (email === 'admin@terrastay.ps' && password === 'admin123') {
      return { token: 'mock-admin-token-xyz', user: MOCK_ADMIN };
    }
    if (password === 'password' || password === 'test123') {
      return { token: 'mock-user-token-abc', user: { ...MOCK_USER, email, name: email.split('@')[0] } };
    }
    throw new Error('البريد الإلكتروني أو كلمة المرور غير صحيحة / Invalid email or password');
  }
};

export const register = async (data) => {
  try {
    const { data: res } = await api.post('/auth/register', data);
    return res;
  } catch {
    await simulateDelay();
    const newUser = {
      id: 'u' + Date.now(),
      name: `${data.firstName} ${data.lastName}`,
      email: data.email,
      role: data.role === 'MANAGER' ? 'ADMIN' : 'GUEST',
    };
    return { token: 'mock-new-user-token', user: newUser };
  }
};

export const logout = async () => {
  try {
    await api.post('/auth/logout');
  } catch {
    // silently fail on logout
  }
};
