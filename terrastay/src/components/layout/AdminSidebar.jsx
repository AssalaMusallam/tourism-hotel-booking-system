import { NavLink } from 'react-router-dom';
import {
  BedDouble,
  Bell,
  CalendarDays,
  ChartColumn,
  CreditCard,
  Hotel,
  LayoutDashboard,
  MessageSquare,
  Settings,
  Sparkles,
  Tag,
  TicketCheck,
  Users,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import styles from './AdminSidebar.module.css';

const adminLinks = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/admin/hotels', label: 'Hotels', icon: Hotel },
  { to: '/admin/room-types', label: 'Room Types', icon: BedDouble },
  { to: '/admin/bookings', label: 'Bookings', icon: CalendarDays },
  { to: '/admin/waiting-list', label: 'Waiting List', icon: TicketCheck },
  { to: '/admin/payments', label: 'Payments', icon: CreditCard },
  { to: '/admin/pricing-rules', label: 'Pricing Rules', icon: Tag },
  { to: '/admin/users', label: 'Guests', icon: Users },
  { to: '/admin/reviews', label: 'Reviews', icon: MessageSquare },
  { to: '/admin/notifications', label: 'Notifications', icon: Bell },
  { to: '/admin/settings', label: 'Settings', icon: Settings },
  { to: '/admin/amenities', label: 'Amenities', icon: Sparkles },
  { to: '/dashboard/reports', label: 'Reports', icon: ChartColumn },
];

const AdminSidebar = () => {
  const { user } = useAuth();
  const isAdminView = user?.role === 'ADMIN' || user?.role === 'MANAGER';

  return (
    <aside className={styles.sidebar}>
      <NavLink to="/admin" className={styles.logo}>
        <span className={styles.logoMark}>T</span>
        <span>TerraStay</span>
      </NavLink>
      <nav className={styles.nav}>
        {isAdminView && adminLinks.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};

export default AdminSidebar;
