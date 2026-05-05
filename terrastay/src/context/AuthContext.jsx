import { createContext, useContext, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import useAuth from '../hooks/useAuth';
import { getMe } from '../api/usersApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const auth = useAuth();
  useQuery({
    queryKey: ['users', 'me'],
    queryFn: async () => {
      const user = await getMe();
      auth.setUser(user);
      return user;
    },
    enabled: Boolean(auth.token),
    staleTime: 5 * 60 * 1000,
    retry: false,
  });

  const value = useMemo(() => ({
    token: auth.token,
    user: auth.user,
    role: auth.user?.role || null,
    login: auth.login,
    logout: auth.logout,
    isAuthenticated: auth.isAuthenticated,
    isAdmin: auth.isAdmin,
    isManager: auth.isManager,
    isGuest: auth.isGuest,
    isActive: auth.isActive,
  }), [auth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuthContext = () => {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return value;
};

export default AuthContext;
