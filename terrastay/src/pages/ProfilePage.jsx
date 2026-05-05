import { useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AlertTriangle, Mail, Phone, User } from 'lucide-react';
import toast from 'react-hot-toast';
import { updateMe } from '../api/usersApi';
import useAuth from '../hooks/useAuth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import RoleBadge from '../components/ui/RoleBadge';
import styles from './ProfilePage.module.css';

const schema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(150, 'Full name must be at most 150 characters'),
  phone: z.string().max(30, 'Phone must be at most 30 characters').optional().or(z.literal('')),
});

const formatDateTime = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString('en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  });
};

const ProfilePage = () => {
  const { user, setUser } = useAuth();
  const queryClient = useQueryClient();

  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      fullName: user?.fullName || '',
      phone: user?.phone || '',
    },
  });

  useEffect(() => {
    reset({ fullName: user?.fullName || '', phone: user?.phone || '' });
  }, [reset, user]);

  const mutation = useMutation({
    mutationFn: updateMe,
    onSuccess: (updated) => {
      setUser(updated);
      queryClient.setQueryData(['users', 'me'], updated);
      toast.success('Profile updated');
    },
    onError: (error) => toast.error(error.response?.data?.message || 'Could not update profile'),
  });

  const onSubmit = (data) => {
    mutation.mutate({
      fullName: data.fullName.trim(),
      phone: data.phone?.trim() || null,
    });
  };

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Account</p>
          <h1>Profile</h1>
        </div>
        <RoleBadge role={user?.role} />
      </div>

      {user?.active === false && (
        <div className={styles.warning}>
          <AlertTriangle size={18} />
          Account Inactive
        </div>
      )}

      <div className={styles.grid}>
        <article className={styles.card}>
          <h2>Account Details</h2>
          <dl className={styles.details}>
            <div><dt>Full Name</dt><dd>{user?.fullName || '-'}</dd></div>
            <div><dt>Email</dt><dd>{user?.email || '-'}</dd></div>
            <div><dt>Phone</dt><dd>{user?.phone || '-'}</dd></div>
            <div><dt>Role</dt><dd><RoleBadge role={user?.role} /></dd></div>
            <div><dt>Created</dt><dd>{formatDateTime(user?.createdAt)}</dd></div>
          </dl>
        </article>

        <article className={styles.card}>
          <h2>Edit Profile</h2>
          <form className={styles.form} onSubmit={handleSubmit(onSubmit)}>
            <Input label="Full Name" icon={User} error={errors.fullName?.message} {...register('fullName')} />
            <Input label="Email" icon={Mail} value={user?.email || ''} disabled />
            <Input label="Phone" icon={Phone} error={errors.phone?.message} {...register('phone')} />
            <Button type="submit" variant="primary" loading={mutation.isPending} disabled={!isDirty}>
              Save Changes
            </Button>
          </form>
        </article>
      </div>
    </section>
  );
};

export default ProfilePage;
