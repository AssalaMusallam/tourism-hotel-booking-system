import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock, User, Phone } from 'lucide-react';
import { useRegister } from '../../hooks/useAuthMutations';
import { useFormErrors } from '../../hooks/useFormErrors';
import Input from '../ui/Input';
import Button from '../ui/Button';
import ErrorBanner from '../ui/ErrorBanner';
import styles from './AuthForm.module.css';

// Zod schema mirroring backend validation exactly
const schema = z.object({
  fullName: z.string().trim().min(1, 'Full name is required').max(150, 'Max 150 characters'),
  email:    z.string().email('Please enter a valid email').max(200, 'Max 200 characters'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .max(100, 'Password must be at most 100 characters')
    .regex(/[A-Z]/, 'Must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Must contain at least one digit'),
  phone: z.string().max(30, 'Max 30 characters').optional().or(z.literal('')),
});

// ── Password strength meter ───────────────────────────────────────────────────

const calcStrength = (pw) => {
  if (!pw || pw.length === 0)   return 0;
  if (pw.length < 8)            return 1;
  const ok = /[A-Z]/.test(pw) && /[a-z]/.test(pw) && /[0-9]/.test(pw);
  if (!ok)                      return 2;
  if (pw.length < 12)           return 3;
  return 4;
};

const STRENGTH_COLOR = ['', '#ef4444', '#f97316', '#f59e0b', '#22c55e'];
const STRENGTH_LABEL = ['', 'Too short', 'Weak', 'Good', 'Strong'];

const StrengthMeter = ({ password }) => {
  const strength = calcStrength(password);
  if (!password) return null;
  const color = STRENGTH_COLOR[strength];
  return (
    <div className={styles.strengthWrap}>
      <div className={styles.strengthBars}>
        {[1, 2, 3, 4].map((lvl) => (
          <div
            key={lvl}
            className={styles.strengthBar}
            style={{ background: strength >= lvl ? color : '#e5e7eb' }}
          />
        ))}
      </div>
      <span className={styles.strengthLabel} style={{ color }}>
        {STRENGTH_LABEL[strength]}
      </span>
    </div>
  );
};

// ── Form ─────────────────────────────────────────────────────────────────────

/**
 * Registration form with password strength meter and inline error banner.
 * Auto-logs in on success via useRegister() mutation.
 */
const RegisterForm = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [bannerError, setBannerError] = useState('');
  const [bannerStatus, setBannerStatus] = useState(null);
  const registerMutation = useRegister();

  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { fullName: '', email: '', password: '', phone: '' },
  });
  const { register, handleSubmit, watch, formState: { errors } } = form;
  const { applyServerErrors } = useFormErrors(form);

  const passwordValue = watch('password');

  const onSubmit = (data) => {
    setBannerError('');
    setBannerStatus(null);
    registerMutation.mutate({
      fullName: data.fullName.trim(),
      email:    data.email.trim().toLowerCase(),
      password: data.password,
      phone:    data.phone?.trim() || undefined,
    }, {
      onError: (error) => {
        const { bannerMessage, status } = applyServerErrors(error);
        setBannerError(bannerMessage);
        setBannerStatus(status);
      },
    });
  };

  return (
    <div className={styles.formWrap}>
      <div className={styles.header}>
        <span className={styles.hotelIcon}>🏨</span>
        <h1 className={styles.title}>Create Account</h1>
        <p className={styles.subtitle}>Join us and start booking</p>
      </div>

      <ErrorBanner message={bannerError} onDismiss={() => setBannerError('')}>
        {bannerStatus === 409 && (
          <Link to="/login" className={styles.bannerLink}>
            Already have an account? Sign in
          </Link>
        )}
      </ErrorBanner>

      <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
        <Input
          label="Full Name"
          icon={User}
          placeholder="Jane Smith"
          error={errors.fullName?.message}
          {...register('fullName')}
        />

        <Input
          label="Email Address"
          type="email"
          autoComplete="email"
          icon={Mail}
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
        />

        <div>
          <Input
            label="Password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            icon={Lock}
            placeholder="Min 8 chars, 1 upper, 1 lower, 1 digit"
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
          <StrengthMeter password={passwordValue} />
        </div>

        <Input
          label="Phone (optional)"
          type="tel"
          icon={Phone}
          placeholder="+970-2-XXX-XXXX"
          error={errors.phone?.message}
          {...register('phone')}
        />

        <Button
          type="submit"
          variant="primary"
          fullWidth
          size="lg"
          loading={registerMutation.isPending}
        >
          Create Account
        </Button>
      </form>

      <div className={styles.footer}>
        <span>Already have an account?</span>
        <Link to="/login" className={styles.switchLink}>Sign in →</Link>
      </div>
    </div>
  );
};

export default RegisterForm;
