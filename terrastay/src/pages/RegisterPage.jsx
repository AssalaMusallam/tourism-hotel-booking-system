import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock, User, Phone } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { register as registerApi } from '../api/authApi';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import styles from './AuthPage.module.css';

// Backend: min8, 1upper, 1lower, 1digit
const schema = z.object({
  fullName: z.string().min(2, 'Full name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Must contain at least one digit'),
  confirmPassword: z.string(),
  phone: z.string().optional().or(z.literal('')),
}).refine((d) => d.password === d.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

const RegisterPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    try {
      // Backend RegisterRequest: { fullName, email, password, phone? }
      const res = await registerApi({
        fullName: data.fullName,
        email: data.email,
        password: data.password,
        phone: data.phone || undefined,
      });
      login(res.token, res.user);
      navigate('/');
      toast.success(`Welcome to TerraStay, ${res.user.fullName?.split(' ')[0] || 'User'}!`);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Registration failed.';
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
          <h1 className={styles.title}>Create Account</h1>
          <p className={styles.subtitle}>Join us today</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
          <Input
            label="Full Name *"
            icon={User}
            error={errors.fullName?.message}
            placeholder="John Doe"
            {...register('fullName')}
          />

          <Input
            label="Email Address *"
            type="email"
            autoComplete="email"
            icon={Mail}
            error={errors.email?.message}
            placeholder="you@example.com"
            {...register('email')}
          />

          <Input
            label="Phone (optional)"
            type="tel"
            icon={Phone}
            error={errors.phone?.message}
            placeholder="+970-2-XXX-XXXX"
            {...register('phone')}
          />

          <Input
            label="Password *"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            icon={Lock}
            error={errors.password?.message}
            placeholder="Min 8 chars, 1 upper, 1 lower, 1 digit"
            hint="Min 8 characters with uppercase, lowercase, and digit"
            rightElement={
              <button type="button" onClick={() => setShowPassword((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('password')}
          />

          <Input
            label="Confirm Password *"
            type={showConfirm ? 'text' : 'password'}
            autoComplete="new-password"
            icon={Lock}
            error={errors.confirmPassword?.message}
            placeholder="Repeat your password"
            rightElement={
              <button type="button" onClick={() => setShowConfirm((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('confirmPassword')}
          />

          <Button type="submit" variant="primary" fullWidth loading={isSubmitting} size="lg">
            Create Account
          </Button>
        </form>

        <div className={styles.footer}>
          <span>Already have an account?</span>
          <Link to="/login" className={styles.switchLink}>Sign In</Link>
        </div>
      </motion.div>
    </div>
  );
};

export default RegisterPage;
