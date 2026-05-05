import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock, User } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { register as apiRegister } from '../api/auth';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Button from '../components/ui/Button';
import styles from './AuthPage.module.css';

const schema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
  role: z.enum(['GUEST', 'MANAGER']),
}).refine((d) => d.password === d.confirmPassword, {
  message: 'Passwords do not match / كلمات المرور غير متطابقة',
  path: ['confirmPassword'],
});

const RegisterPage = () => {
  const { setAuth } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { role: 'GUEST' },
  });

  const onSubmit = async (data) => {
    try {
      const res = await apiRegister(data);
      setAuth(res.user, res.token);
      navigate('/');
      toast.success(`Welcome to TerraStay, ${res.user.name.split(' ')[0]}!`);
    } catch (err) {
      toast.error(err.message || 'Registration failed. Please try again.');
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
          <h1 className={styles.title}>Create Account</h1>
          <p className={styles.subtitle}>إنشاء حساب — Join us today</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
          <div className={styles.rowFields}>
            <Input
              label="First Name"
              leftIcon={User}
              error={errors.firstName?.message}
              placeholder="Ahmad"
              {...register('firstName')}
            />
            <Input
              label="Last Name"
              error={errors.lastName?.message}
              placeholder="Khalil"
              {...register('lastName')}
            />
          </div>

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
            autoComplete="new-password"
            leftIcon={Lock}
            error={errors.password?.message}
            placeholder="At least 6 characters"
            rightElement={
              <button type="button" onClick={() => setShowPassword((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('password')}
          />

          <Input
            label="Confirm Password"
            type={showConfirm ? 'text' : 'password'}
            autoComplete="new-password"
            leftIcon={Lock}
            error={errors.confirmPassword?.message}
            placeholder="Repeat your password"
            rightElement={
              <button type="button" onClick={() => setShowConfirm((v) => !v)} className={styles.eyeBtn} tabIndex={-1}>
                {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            }
            {...register('confirmPassword')}
          />

          <Select
            label="I am a..."
            options={[
              { value: 'GUEST', label: 'Traveler / Guest (أريد حجز فندق)' },
              { value: 'MANAGER', label: 'Hotel Manager (أريد إدارة فندق)' },
            ]}
            error={errors.role?.message}
            {...register('role')}
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
