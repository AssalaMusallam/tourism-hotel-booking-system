import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Hotel, Sparkles, Plus, Users, Tag, BarChart2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { useAdminHotels, useAmenities } from '../../hooks/useCatalogQueries';
import useAuth from '../../hooks/useAuth';
import Spinner from '../../components/ui/Spinner';
import styles from './AdminDashboard.module.css';

const StatCard = ({ title, value, icon: Icon, color = 'terracotta' }) => (
  <div className={styles.statCard} style={{ borderLeftColor: `var(--color-${color})` }}>
    <div className={styles.statIcon}>
      <Icon size={22} />
    </div>
    <div>
      <div className={styles.statValue}>{value}</div>
      <div className={styles.statTitle}>{title}</div>
    </div>
  </div>
);

const AdminDashboard = () => {
  const { user, isAdmin } = useAuth();
  useEffect(() => { document.title = 'Dashboard – PinkFlow'; }, []);

  const { data: hotelsData, isLoading: hotelsLoading } = useAdminHotels({ page: 0, size: 1 });
  const { data: amenitiesData, isLoading: amenitiesLoading } = useAmenities({ page: 0, size: 1 });

  const totalHotels = hotelsData?.totalElements || 0;
  const totalAmenities = amenitiesData?.totalElements || 0;

  const isLoading = hotelsLoading || amenitiesLoading;

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/dashboard" className={`${styles.navLink} ${styles.active}`}>Dashboard</Link>
          <Link to="/dashboard/hotels" className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/amenities" className={styles.navLink}>Manage Amenities</Link>
          {isAdmin && <Link to="/dashboard/users" className={styles.navLink}>Manage Users</Link>}
          <Link to="/dashboard/pricing-rules" className={styles.navLink}>Pricing Rules</Link>
          {isAdmin && <Link to="/dashboard/reports" className={styles.navLink}>Analytics</Link>}
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <div>
            <h1>Welcome, {user?.fullName?.split(' ')[0] || 'Admin'}</h1>
            <p style={{ color: 'var(--color-text-muted)', marginTop: 4 }}>
              Manage your hotels, rooms, and amenities
            </p>
          </div>
          <div className={styles.quickActions}>
            <Link to="/dashboard/hotels" className={styles.actionBtn}>
              <Plus size={16} /> Manage Hotels
            </Link>
          </div>
        </div>

        {isLoading ? <Spinner centered /> : (
          <div className={styles.statsGrid}>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0 }}>
              <StatCard title="Total Hotels" value={totalHotels} icon={Hotel} />
            </motion.div>
            <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.08 }}>
              <StatCard title="Amenities" value={totalAmenities} icon={Sparkles} color="beige" />
            </motion.div>
          </div>
        )}

        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>Quick Links</h2>
          <div className={styles.quickLinksGrid}>
            <Link to="/dashboard/hotels" className={styles.quickLink}>
              <Hotel size={20} />
              <span>Manage Hotels</span>
              <p>View, create, edit, and manage all hotels</p>
            </Link>
            <Link to="/admin/amenities" className={styles.quickLink}>
              <Sparkles size={20} />
              <span>Manage Amenities</span>
              <p>Create and configure amenities for hotels and rooms</p>
            </Link>
            {isAdmin && (
              <Link to="/dashboard/users" className={styles.quickLink}>
                <Users size={20} />
                <span>Manage Users</span>
                <p>Change roles, active status, and manager hotel access</p>
              </Link>
            )}
            <Link to="/dashboard/pricing-rules" className={styles.quickLink}>
              <Tag size={20} />
              <span>Pricing Rules</span>
              <p>Configure seasonal multipliers and price adjustments</p>
            </Link>
            {isAdmin && (
              <Link to="/dashboard/reports" className={styles.quickLink}>
                <BarChart2 size={20} />
                <span>Analytics &amp; Reports</span>
                <p>Revenue, occupancy, and booking insights per hotel</p>
              </Link>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
