import useAuthStore from '../store/authStore';

const useAuth = () => {
  const { user, token, isAuthenticated, setAuth, clearAuth } = useAuthStore();
  const isAdmin = user?.role === 'ADMIN';
  const isGuest = user?.role === 'GUEST';

  return { user, token, isAuthenticated, isAdmin, isGuest, setAuth, clearAuth };
};

export default useAuth;
