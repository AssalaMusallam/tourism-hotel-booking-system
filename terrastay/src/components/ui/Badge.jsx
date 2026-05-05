import styles from './Badge.module.css';

const Badge = ({ children, variant = 'default' }) => (
  <span className={[styles.badge, styles[variant]].join(' ')}>{children}</span>
);

// Convenience status badge that maps status strings to badge variants
export const StatusBadge = ({ status }) => {
  const map = {
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    UNDER_MAINTENANCE: 'maintenance',
    true: 'active',
    false: 'inactive',
  };
  const labels = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    UNDER_MAINTENANCE: 'Maintenance',
    true: 'Active',
    false: 'Inactive',
  };
  const key = String(status);
  return <Badge variant={map[key] || 'default'}>{labels[key] || key}</Badge>;
};

export default Badge;
