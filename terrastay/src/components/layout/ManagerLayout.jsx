import { NavLink } from 'react-router-dom';
import { Bell, BedDouble, CalendarDays, CreditCard, Hotel, LayoutDashboard, MessageSquare, Settings, TicketCheck } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import ThemeToggle from '../ui/ThemeToggle';
import LanguageToggle from '../ui/LanguageToggle';
import CurrencySelector from '../CurrencySelector';
import styles from './ManagerLayout.module.css';

const links = [
  { to: '/manager/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/manager/hotel', label: 'My Hotel', icon: Hotel },
  { to: '/manager/room-types', label: 'Room Types', icon: BedDouble },
  { to: '/manager/bookings', label: 'Bookings', icon: CalendarDays },
  { to: '/manager/waiting-list', label: 'Waiting List', icon: TicketCheck },
  { to: '/manager/payments', label: 'Payments', icon: CreditCard },
  { to: '/manager/reviews', label: 'Reviews', icon: MessageSquare },
  { to: '/manager/notifications', label: 'Notifications', icon: Bell },
  { to: '/manager/settings', label: 'Settings', icon: Settings },
];

const ManagerLayout = ({ title = 'Manager', children }) => {
  const { user } = useAuth();

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <NavLink to="/manager/dashboard" className={styles.logo}>
          <span className={styles.logoMark}>T</span>
          <span>TerraStay</span>
        </NavLink>
        <nav className={styles.nav}>
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}>
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className={styles.content}>
        <header className={styles.topbar}>
          <div>
            <span className={styles.eyebrow}>TerraStay Manager</span>
            <h1>{title}</h1>
          </div>
          <div className={styles.actions}>
            <CurrencySelector />
            <LanguageToggle />
            <ThemeToggle />
            <button type="button" className={styles.iconButton} aria-label="Notifications"><Bell size={18} /></button>
            <div className={styles.user}>
              <strong>{user?.fullName || user?.name || 'Manager'}</strong>
              <small>{user?.role || 'MANAGER'}</small>
            </div>
          </div>
        </header>
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
};

export default ManagerLayout;
