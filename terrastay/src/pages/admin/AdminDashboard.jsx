import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Hotel, BedDouble, CalendarCheck, DollarSign, Plus } from 'lucide-react';
import { motion } from 'framer-motion';
import { getAdminStats } from '../../api/admin';
import { getAllBookings } from '../../api/bookings';
import StatsCard from '../../components/admin/StatsCard';
import Badge from '../../components/ui/Badge';
import Spinner from '../../components/ui/Spinner';
import { formatPrice } from '../../utils/formatPrice';
import { formatDate } from '../../utils/formatDate';
import styles from './AdminDashboard.module.css';

const AdminDashboard = () => {
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: getAdminStats,
  });

  const { data: bookingsData, isLoading: bookingsLoading } = useQuery({
    queryKey: ['admin-bookings'],
    queryFn: () => getAllBookings(),
  });

  const recentBookings = bookingsData?.data?.slice(0, 10) || [];

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={`${styles.navLink} ${styles.active}`}>Dashboard</Link>
          <Link to="/admin/hotels" className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/bookings" className={styles.navLink}>All Bookings</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <h1>Admin Dashboard</h1>
          <div className={styles.quickActions}>
            <Link to="/admin/hotels" className={styles.actionBtn}>
              <Plus size={16} /> Add Hotel
            </Link>
          </div>
        </div>

        {statsLoading ? <Spinner centered /> : (
          <div className={styles.statsGrid}>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0 }}>
              <StatsCard title="Total Hotels" value={stats?.totalHotels || 0} icon={Hotel} />
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.08 }}>
              <StatsCard title="Total Rooms" value={stats?.totalRooms || 0} icon={BedDouble} color="beige" />
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.16 }}>
              <StatsCard title="Bookings This Month" value={stats?.bookingsThisMonth || 0} icon={CalendarCheck} />
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.24 }}>
              <StatsCard
                title="Revenue This Month"
                value={formatPrice(stats?.revenueThisMonth || 0)}
                icon={DollarSign}
                color="red"
                trend={`Total: ${formatPrice(stats?.totalRevenue || 0)}`}
              />
            </motion.div>
          </div>
        )}

        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Recent Bookings</h2>
          {bookingsLoading ? <Spinner centered /> : (
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Booking ID</th>
                    <th>Guest</th>
                    <th>Hotel</th>
                    <th>Check-in</th>
                    <th>Total</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recentBookings.map((b) => (
                    <tr key={b.id}>
                      <td className={styles.id}>#{b.id}</td>
                      <td>{b.guestName}</td>
                      <td className={styles.hotelCell}>{b.hotelName}</td>
                      <td>{formatDate(b.checkIn)}</td>
                      <td className={styles.price}>{formatPrice(b.totalPrice)}</td>
                      <td><Badge status={b.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
