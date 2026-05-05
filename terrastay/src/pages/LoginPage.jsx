import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { login as loginApi } from '../api/authApi';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import styles from './AuthPage.module.css';

const schema = z.object({
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || '/';
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    try {
      const res = await loginApi({ email: data.email, password: data.password });
      // AuthResponse: { token, type, user: { id, fullName, email, role, phone } }
      login(res.token, res.user);

      // Check for pendingBooking in sessionStorage
      const pending = sessionStorage.getItem('pendingBooking');
      if (pending) {
        const { hotelId } = JSON.parse(pending);
        sessionStorage.removeItem('pendingBooking');
        navigate(`/hotels/${hotelId}`, { replace: true });
      } else {
        navigate(from, { replace: true });
      }
      toast.success(`Welcome back, ${res.user.fullName?.split(' ')[0] || 'User'}!`);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Login failed. Please try again.';
      toast.error(msg);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.bg} />
      <motion.div
        className={styles.card}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
      >
        <div className={styles.header}>
          <Link to="/" className={styles.logo}>TerraStay</Link>
          <h1 className={styles.title}>Welcome Back</h1>
          <p className={styles.subtitle}>Sign in to your account</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
          <Input
            label="Email Address"
            type="email"
            autoComplete="email"
            icon={Mail}
            error={errors.email?.message}
            placeholder="you@example.com"
            {...register('email')}
          />

          <Input
            label="Password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            icon={Lock}
            error={errors.password?.message}
            placeholder="••••••••"
            rightElement={
              <button type="button" onClick={() => setShowPassword((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('password')}
          />

          <Button type="submit" variant="primary" fullWidth loading={isSubmitting} size="lg">
            Sign In
          </Button>
        </form>

        <div className={styles.footer}>
          <span>Don't have an account?</span>
          <Link to="/register" className={styles.switchLink}>Create Account</Link>
        </div>
      </motion.div>
    </div>
  );
};

export default LoginPage;
