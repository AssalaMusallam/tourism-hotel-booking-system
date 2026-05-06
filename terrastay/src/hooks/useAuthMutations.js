import { useMutation } from '@tanstack/react-query';
import { useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { login as loginApi, register as registerApi } from '../api/authApi';
import useAuth from './useAuth';

/**
 * React Query mutation for login.
 *
 * onSuccess: saves auth state, redirects based on role + location.state.from.
 * Errors are NOT handled here — components read mutation.isError for inline banners.
 *
 * @returns {UseMutationResult}
 */
export const useLogin = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  return useMutation({
    mutationFn: (credentials) =>
      loginApi({ email: credentials.email.trim().toLowerCase(), password: credentials.password }),

    onSuccess: (data) => {
      login(data.token, data.user);

      // Restore pending booking flow if any
      const pending = sessionStorage.getItem('pendingBooking');
      if (pending) {
        try {
          const { hotelId } = JSON.parse(pending);
          sessionStorage.removeItem('pendingBooking');
          navigate(`/hotels/${hotelId}`, { replace: true });
        } catch {
          sessionStorage.removeItem('pendingBooking');
        }
        return;
      }

      // Redirect to state.from or role-based default
      const from = location.state?.from;
      const isAdminRole = data.user?.role === 'ADMIN' || data.user?.role === 'MANAGER';
      const roleHome = isAdminRole ? '/admin' : '/';
      const target = isAdminRole ? roleHome : (from && from !== '/login' && from !== '/register' ? from : roleHome);
      navigate(target, { replace: true });

      toast.success(`Welcome back, ${data.user?.fullName?.split(' ')[0] || 'User'}!`);
    },
  });
};

/**
 * React Query mutation for registration.
 *
 * onSuccess: auto-logs in and redirects to home with welcome toast.
 * Errors are NOT handled here — components read mutation.isError for inline banners.
 *
 * @returns {UseMutationResult}
 */
export const useRegister = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: registerApi,

    onSuccess: (data) => {
      login(data.token, data.user);
      navigate('/', { replace: true });
      toast.success(`Welcome, ${data.user?.fullName?.split(' ')[0] || 'User'}! Your account is ready.`);
    },
  });
};
