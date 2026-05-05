import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Building2, Search } from 'lucide-react';
import toast from 'react-hot-toast';
import { getManagedHotelsForUser, getUsers, updateUserRole, updateUserStatus } from '../../api/usersApi';
import Button from '../../components/ui/Button';
import EmptyState from '../../components/ui/EmptyState';
import RoleBadge from '../../components/ui/RoleBadge';
import Spinner from '../../components/ui/Spinner';
import useAuth from '../../hooks/useAuth';
import styles from './ManageUsers.module.css';

const ROLES = ['', 'GUEST', 'MANAGER', 'ADMIN'];
const ACTIVE_OPTIONS = [
  { value: '', label: 'All statuses' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' },
];

const formatDate = (value) => value
  ? new Date(value).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
  : '-';

const ManageUsers = () => {
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [role, setRole] = useState('');
  const [active, setActive] = useState('');
  const [managerId, setManagerId] = useState(null);

  const params = {
    role: role || undefined,
    active: active === '' ? undefined : active === 'true',
  };

  const usersQuery = useQuery({
    queryKey: ['admin', 'users', params],
    queryFn: () => getUsers(params),
    enabled: isAdmin,
  });

  const hotelsQuery = useQuery({
    queryKey: ['admin', 'users', managerId, 'hotels'],
    queryFn: () => getManagedHotelsForUser(managerId),
    enabled: Boolean(managerId),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, nextActive }) => updateUserStatus(id, nextActive),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User status updated');
    },
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, nextRole }) => updateUserRole(id, nextRole),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User role updated');
    },
  });

  const toggleStatus = (user) => {
    const nextActive = !user.active;
    if (!nextActive && !window.confirm(`Deactivate ${user.fullName}?`)) return;
    statusMutation.mutate({ id: user.id, nextActive });
  };

  if (!isAdmin) {
    return (
      <section className={styles.page}>
        <EmptyState title="You don't have permission" description="Only admins can manage users." />
      </section>
    );
  }

  const users = usersQuery.data || [];
  const selectedManager = users.find((user) => user.id === managerId);

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Dashboard</p>
          <h1>User Management</h1>
        </div>
      </div>

      <div className={styles.filters}>
        <label>
          <span>Role</span>
          <select value={role} onChange={(event) => setRole(event.target.value)}>
            {ROLES.map((item) => <option key={item || 'all'} value={item}>{item || 'All roles'}</option>)}
          </select>
        </label>
        <label>
          <span>Status</span>
          <select value={active} onChange={(event) => setActive(event.target.value)}>
            {ACTIVE_OPTIONS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
          </select>
        </label>
      </div>

      {usersQuery.isLoading ? <Spinner centered /> : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Active</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>#{user.id}</td>
                  <td>{user.fullName}</td>
                  <td>{user.email}</td>
                  <td>{user.phone || '-'}</td>
                  <td>
                    <select
                      className={styles.roleSelect}
                      value={user.role}
                      onChange={(event) => roleMutation.mutate({ id: user.id, nextRole: event.target.value })}
                    >
                      <option value="GUEST">GUEST</option>
                      <option value="MANAGER">MANAGER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td>
                    <span className={user.active ? styles.active : styles.inactive}>
                      {user.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>{formatDate(user.createdAt)}</td>
                  <td>
                    <div className={styles.actions}>
                      <Button size="sm" variant={user.active ? 'danger' : 'secondary'} onClick={() => toggleStatus(user)}>
                        {user.active ? 'Deactivate' : 'Activate'}
                      </Button>
                      {user.role === 'MANAGER' && (
                        <Button size="sm" variant="ghost" icon={Building2} onClick={() => setManagerId(user.id)}>
                          Hotels
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {users.length === 0 && <EmptyState icon={Search} title="No users found" description="Try changing the filters." />}
        </div>
      )}

      {managerId && (
        <div className={styles.overlay} onClick={() => setManagerId(null)}>
          <div className={styles.dialog} onClick={(event) => event.stopPropagation()}>
            <div className={styles.dialogHeader}>
              <div>
                <h2>{selectedManager?.fullName}</h2>
                <RoleBadge role="MANAGER" />
              </div>
              <button type="button" onClick={() => setManagerId(null)}>Close</button>
            </div>
            {hotelsQuery.isLoading ? <Spinner centered /> : (
              <div className={styles.hotelList}>
                {(hotelsQuery.data || []).map((hotel) => (
                  <Link key={hotel.id} to={`/dashboard/hotels/${hotel.id}/managers`}>
                    {hotel.name}
                  </Link>
                ))}
                {(hotelsQuery.data || []).length === 0 && <p>No managed hotels assigned.</p>}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
};

export default ManageUsers;
