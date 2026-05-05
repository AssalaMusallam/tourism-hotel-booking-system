import styles from './RoleBadge.module.css';

const labels = {
  GUEST: 'Guest',
  MANAGER: 'Manager',
  ADMIN: 'Admin',
};

const RoleBadge = ({ role }) => {
  const key = role || 'GUEST';
  return <span className={`${styles.badge} ${styles[key.toLowerCase()]}`}>{labels[key] || key}</span>;
};

export default RoleBadge;
