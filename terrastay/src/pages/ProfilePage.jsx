import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AlertTriangle, Mail, Phone, User } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '../api/axios';
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
  const [managerModalOpen, setManagerModalOpen] = useState(false);

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

  const managerRequestMutation = useMutation({
    mutationFn: async (event) => {
      event.preventDefault();
      const payload = Object.fromEntries(new FormData(event.currentTarget));
      try {
        return await api.post('/api/users/request-manager', payload).then((response) => response.data);
      } catch {
        return api.post('/api/manager/request', payload).then((response) => response.data);
      }
    },
    onSuccess: () => {
      toast.success('Your request has been submitted. Admin will review it shortly.');
      setManagerModalOpen(false);
    },
    onError: (error) => toast.error(error.response?.data?.message || 'Could not submit manager request'),
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

        {user?.role !== 'MANAGER' && user?.role !== 'ADMIN' && (
          <article className={styles.card}>
            <h2>Become a Hotel Manager</h2>
            <p className={styles.helperText}>Submit your hotel details for admin review.</p>
            <Button type="button" variant="secondary" onClick={() => setManagerModalOpen(true)}>
              Request Manager Role
            </Button>
          </article>
        )}
      </div>

      {managerModalOpen && (
        <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => setManagerModalOpen(false)}>
          <div className={styles.modal} role="dialog" aria-modal="true" aria-label="Request manager role" onMouseDown={(event) => event.stopPropagation()}>
            <h2>Request Manager Role</h2>
            <form className={styles.managerForm} onSubmit={(event) => managerRequestMutation.mutate(event)}>
              <label>Hotel Name<input name="hotelName" required /></label>
              <label>Hotel City
                <select name="hotelCity" required>
                  {['Jerusalem', 'Bethlehem', 'Ramallah', 'Jericho', 'Nablus', 'Hebron', 'Jenin', 'Tulkarm'].map((city) => <option key={city} value={city}>{city}</option>)}
                </select>
              </label>
              <label>Hotel Address<input name="hotelAddress" required /></label>
              <label>Phone Number<input name="phoneNumber" required /></label>
              <label className={styles.fullWidth}>Short Description<textarea name="description" rows="4" required /></label>
              <div className={styles.modalActions}>
                <Button type="button" variant="ghost" onClick={() => setManagerModalOpen(false)}>Cancel</Button>
                <Button type="submit" variant="primary" loading={managerRequestMutation.isPending}>Submit</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
};

export default ProfilePage;
