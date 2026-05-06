import { NavLink } from 'react-router-dom';
import {
  BedDouble,
  Bell,
  CalendarDays,
  ChartColumn,
  Hotel,
  LayoutDashboard,
  Sparkles,
  Tag,
  TicketCheck,
  Users,
} from 'lucide-react';
import styles from './AdminSidebar.module.css';

const links = [
  { to: '/admin', label: 'لوحة التحكم', icon: LayoutDashboard, end: true },
  { to: '/admin/hotels', label: 'الفنادق', icon: Hotel },
  { to: '/admin/hotels/1/rooms', label: 'أنواع الغرف', icon: BedDouble },
  { to: '/admin/bookings', label: 'الحجوزات', icon: CalendarDays },
  { to: '/admin/users', label: 'المستخدمون', icon: Users },
  { to: '/dashboard/reports', label: 'التقارير', icon: ChartColumn },
  { to: '/admin/pricing-rules', label: 'قواعد التسعير', icon: Tag },
  { to: '/admin/notifications', label: 'الإشعارات', icon: Bell },
  { to: '/admin/role-requests', label: 'طلبات الأدوار', icon: TicketCheck },
  { to: '/admin/amenities', label: 'وسائل الراحة', icon: Sparkles },
];

const AdminSidebar = () => (
  <aside className={styles.sidebar}>
    <NavLink to="/admin" className={styles.logo}>
      <span className={styles.logoMark}>T</span>
      <span>TerraStay</span>
    </NavLink>
    <nav className={styles.nav}>
      {links.map(({ to, label, icon: Icon, end }) => (
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

export default AdminSidebar;
