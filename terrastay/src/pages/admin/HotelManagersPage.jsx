import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Search, UserPlus, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { assignManager, getHotelManagers, removeManager } from '../../api/hotelsApi';
import { getUsers } from '../../api/usersApi';
import Button from '../../components/ui/Button';
import EmptyState from '../../components/ui/EmptyState';
import RoleBadge from '../../components/ui/RoleBadge';
import Spinner from '../../components/ui/Spinner';
import useAuth from '../../hooks/useAuth';
import styles from './HotelManagersPage.module.css';

const managerError = (error) => {
  const message = error.response?.data?.message || '';
  if (error.response?.status === 409) return 'This user is already a manager of this hotel';
  if (message.includes('does not have MANAGER role')) return 'This user does not have the MANAGER role';
  if (message.includes('not a manager of hotel')) return 'This user is not assigned to this hotel';
  return message || 'Manager assignment failed';
};

const HotelManagersPage = () => {
  const { hotelId } = useParams();
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [email, setEmail] = useState('');

  const managersQuery = useQuery({
    queryKey: ['admin', 'hotels', hotelId, 'managers'],
    queryFn: () => getHotelManagers(hotelId),
    enabled: Boolean(hotelId) && isAdmin,
  });

  const usersQuery = useQuery({
    queryKey: ['admin', 'users', 'managers'],
    queryFn: () => getUsers({ role: 'MANAGER', active: true }),
    enabled: isAdmin,
    staleTime: 5 * 60 * 1000,
  });

  const assignMutation = useMutation({
    mutationFn: (userId) => assignManager(hotelId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'hotels', hotelId, 'managers'] });
      toast.success('Manager assigned');
      setEmail('');
    },
    onError: (error) => toast.error(managerError(error)),
  });

  const removeMutation = useMutation({
    mutationFn: (userId) => removeManager(hotelId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'hotels', hotelId, 'managers'] });
      toast.success('Manager removed');
    },
    onError: (error) => toast.error(managerError(error)),
  });

  const candidates = useMemo(() => {
    const needle = email.trim().toLowerCase();
    if (!needle) return [];
    return (usersQuery.data || []).filter((user) => user.email.toLowerCase().includes(needle));
  }, [email, usersQuery.data]);

  if (!isAdmin) {
    return (
      <section className={styles.page}>
        <EmptyState title="You don't have permission" description="Only admins can assign hotel managers." />
      </section>
    );
  }

  const managers = managersQuery.data || [];

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <Link to="/dashboard/hotels" className={styles.back}>Back to hotels</Link>
          <h1>Hotel Managers</h1>
          <p>Hotel #{hotelId}</p>
        </div>
      </div>

      <div className={styles.grid}>
        <article className={styles.card}>
          <h2>Current Managers</h2>
          {managersQuery.isLoading ? <Spinner centered /> : (
            <div className={styles.managerList}>
              {managers.map((manager) => (
                <div key={manager.id} className={styles.managerRow}>
                  <div>
                    <strong>{manager.fullName}</strong>
                    <span>{manager.email}</span>
                    <RoleBadge role={manager.role} />
                  </div>
                  <Button size="sm" variant="danger" icon={X} onClick={() => removeMutation.mutate(manager.id)}>
                    Remove
                  </Button>
                </div>
              ))}
              {managers.length === 0 && <EmptyState title="No managers assigned" description="Search by email to assign one." />}
            </div>
          )}
        </article>

        <article className={styles.card}>
          <h2>Assign Manager</h2>
          <label className={styles.search}>
            <span>Search manager by email</span>
            <div>
              <Search size={18} />
              <input
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="manager@example.com"
              />
            </div>
          </label>

          <div className={styles.candidates}>
            {candidates.map((user) => (
              <button key={user.id} type="button" onClick={() => assignMutation.mutate(user.id)}>
                <span>
                  <strong>{user.fullName}</strong>
                  <small>{user.email}</small>
                </span>
                <UserPlus size={17} />
              </button>
            ))}
            {email && candidates.length === 0 && <p>No matching manager users.</p>}
          </div>
        </article>
      </div>
    </section>
  );
};

export default HotelManagersPage;
