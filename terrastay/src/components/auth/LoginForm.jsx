import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock } from 'lucide-react';
import { useLogin } from '../../hooks/useAuthMutations';
import { useFormErrors } from '../../hooks/useFormErrors';
import Input from '../ui/Input';
import Button from '../ui/Button';
import ErrorBanner from '../ui/ErrorBanner';
import styles from './AuthForm.module.css';

const schema = z.object({
  email:    z.string().email('Please enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

/**
 * Login form with show/hide password toggle and inline error banner.
 * Uses useLogin() mutation — no toast-only error handling.
 */
const LoginForm = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [bannerError, setBannerError] = useState('');
  const loginMutation = useLogin();

  const form = useForm({
    resolver: zodResolver(schema),
  });
  const { register, handleSubmit, formState: { errors } } = form;
  const { applyServerErrors } = useFormErrors(form);

  const onSubmit = (data) => {
    setBannerError('');
    loginMutation.mutate(data, {
      onError: (error) => {
        const { bannerMessage } = applyServerErrors(error);
        setBannerError(bannerMessage);
      },
    });
  };

  return (
    <div className={styles.formWrap}>
      <div className={styles.header}>
        <span className={styles.hotelIcon}>🏨</span>
        <h1 className={styles.title}>Welcome Back</h1>
        <p className={styles.subtitle}>Sign in to your account</p>
      </div>

      <ErrorBanner message={bannerError} onDismiss={() => setBannerError('')} />

      <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
        <Input
          label="Email Address"
          type="email"
          autoComplete="email"
          icon={Mail}
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Password"
          type={showPassword ? 'text' : 'password'}
          autoComplete="current-password"
          icon={Lock}
          placeholder="••••••••"
          error={errors.password?.message}
          rightElement={
            <button
              type="button"
              className={styles.eyeBtn}
              onClick={() => setShowPassword((v) => !v)}
              tabIndex={-1}
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            >
              {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          }
          {...register('password')}
        />

        <Button
          type="submit"
          variant="primary"
          fullWidth
          size="lg"
          loading={loginMutation.isPending}
        >
          Sign In
        </Button>
      </form>

      <div className={styles.footer}>
        <span>Don&rsquo;t have an account?</span>
        <Link to="/register" className={styles.switchLink}>Create one →</Link>
      </div>
    </div>
  );
};

export default LoginForm;
