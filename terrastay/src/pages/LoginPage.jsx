import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { login } from '../api/auth';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import styles from './AuthPage.module.css';

const schema = z.object({
  email: z.string().email('Please enter a valid email / أدخل بريدًا إلكترونيًا صحيحًا'),
  password: z.string().min(6, 'Password must be at least 6 characters / كلمة المرور يجب أن تكون 6 أحرف على الأقل'),
  remember: z.boolean().optional(),
});

const LoginPage = () => {
  const { setAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || '/';
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    try {
      const res = await login(data);
      setAuth(res.user, res.token);

      const pending = sessionStorage.getItem('pendingBooking');
      if (pending) {
        const { hotelId } = JSON.parse(pending);
        navigate(`/booking/${hotelId}`, { replace: true });
      } else {
        navigate(from, { replace: true });
      }
      toast.success(`Welcome back, ${res.user.name.split(' ')[0]}!`);
    } catch (err) {
      toast.error(err.message || 'Login failed. Please try again.');
    }
  };

  return (
    <div className={styles.page}>
      <div className={`${styles.bg} embroidery-pattern`} />
      <motion.div
        className={styles.card}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
      >
        <div className={styles.header}>
          <Link to="/" className={styles.logo}>TerraStay</Link>
          <h1 className={styles.title}>Welcome Back</h1>
          <p className={styles.subtitle}>مرحباً بعودتك — Sign in to your account</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
          <Input
            label="Email Address"
            type="email"
            autoComplete="email"
            leftIcon={Mail}
            error={errors.email?.message}
            placeholder="you@example.com"
            {...register('email')}
          />

          <Input
            label="Password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            leftIcon={Lock}
            error={errors.password?.message}
            placeholder="••••••••"
            rightElement={
              <button type="button" onClick={() => setShowPassword((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('password')}
          />

          <div className={styles.formMeta}>
            <label className={styles.rememberLabel}>
              <input type="checkbox" {...register('remember')} className={styles.checkbox} />
              Remember me
            </label>
            <Link to="/forgot-password" className={styles.forgotLink}>Forgot password?</Link>
          </div>

          <Button type="submit" variant="primary" fullWidth loading={isSubmitting} size="lg">
            Sign In
          </Button>
        </form>

        <div className={styles.footer}>
          <span>Don't have an account?</span>
          <Link to="/register" className={styles.switchLink}>Create Account</Link>
        </div>

        <div className={styles.hint}>
          <small>Demo: any email + "password" | Admin: admin@terrastay.ps / admin123</small>
        </div>
      </motion.div>
    </div>
  );
};

export default LoginPage;
