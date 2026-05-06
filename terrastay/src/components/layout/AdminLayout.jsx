import { Bell } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import ThemeToggle from '../ui/ThemeToggle';
import LanguageToggle from '../ui/LanguageToggle';
import CurrencySelector from '../CurrencySelector';
import AdminSidebar from './AdminSidebar';
import styles from './AdminLayout.module.css';

const AdminLayout = ({ title = 'لوحة التحكم', children }) => {
  const { user } = useAuth();

  return (
    <div className={styles.shell}>
      <AdminSidebar />
      <div className={styles.content}>
        <header className={styles.topbar}>
          <div>
            <span className={styles.eyebrow}>TerraStay Admin</span>
            <h1>{title}</h1>
          </div>
          <div className={styles.actions}>
            <CurrencySelector />
            <LanguageToggle />
            <ThemeToggle />
            <button type="button" className={styles.iconButton} aria-label="Notifications">
              <Bell size={18} />
              <span />
            </button>
            <div className={styles.user}>
              <strong>{user?.fullName || user?.name || 'Admin'}</strong>
              <small>{user?.role || 'ADMIN'}</small>
            </div>
          </div>
        </header>
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
};

export default AdminLayout;
