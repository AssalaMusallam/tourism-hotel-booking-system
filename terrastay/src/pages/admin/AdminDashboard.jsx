import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BarChart3, CalendarClock, DollarSign, Hotel, Percent, Plus, Tag } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import {
  useAdminBookingStatus,
  useAdminPopularRooms,
  useAdminRecentBookings,
  useAdminRevenue,
  useAdminSummary,
} from '../../hooks/useAdminQueries';
import api from '../../api/axios';
import '../../styles/admin-theme.css';
import styles from './AdminDashboard.module.css';

const number = (value) => Number(value || 0);

const KpiCard = ({ title, value, icon: Icon, tone = 'primary' }) => (
  <article className={`${styles.kpiCard} ${styles[tone]}`}>
    <Icon size={22} />
    <div>
      <strong>{value}</strong>
      <span>{title}</span>
    </div>
  </article>
);

const statusClass = (status) => ({
  CONFIRMED: styles.confirmed,
  PENDING: styles.pending,
  CANCELLED: styles.cancelled,
  COMPLETED: styles.completed,
}[status] || styles.completed);

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const summaryQuery = useAdminSummary();
  const revenueQuery = useAdminRevenue();
  const statusQuery = useAdminBookingStatus();
  const bookingsQuery = useAdminRecentBookings();
  const roomsQuery = useAdminPopularRooms();

  useEffect(() => {
    document.title = 'لوحة التحكم - TerraStay';
  }, []);

  const summary = summaryQuery.data || {};
  const revenue = Array.isArray(revenueQuery.data) ? revenueQuery.data : [];
  const statuses = Array.isArray(statusQuery.data) ? statusQuery.data : [];
  const bookings = bookingsQuery.data?.content || bookingsQuery.data || [];
  const rooms = Array.isArray(roomsQuery.data) ? roomsQuery.data : [];
  const maxRevenue = Math.max(...revenue.map((item) => number(item.revenue || item.totalRevenue || item.amount)), 1);
  const statusTotal = Math.max(statuses.reduce((sum, item) => sum + number(item.count || item.value), 0), 1);

  const exportReport = async () => {
    const response = await api.get('/api/admin/reports/export', { responseType: 'blob' });
    const blob = response.data;
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'terrastay-admin-report';
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <section className={`admin-layout ${styles.dashboard}`}>
      <header className={styles.header}>
        <div>
          <h1>لوحة التحكم</h1>
          <p>مرحباً، {user?.name || user?.fullName || 'Admin'}</p>
        </div>
      </header>

      <div className={styles.kpiGrid}>
        <KpiCard title="إجمالي الحجوزات هذا الشهر" value={number(summary.monthlyBookings || summary.totalBookingsThisMonth)} icon={CalendarClock} />
        <KpiCard title="إجمالي الإيرادات ($)" value={`$${number(summary.totalRevenue || summary.monthlyRevenue).toLocaleString()}`} icon={DollarSign} tone="success" />
        <KpiCard title="نسبة الإشغال (%)" value={`${number(summary.occupancyRate).toFixed(0)}%`} icon={Percent} tone="warn" />
        <KpiCard title="الحجوزات المعلقة" value={number(summary.pendingBookings)} icon={Hotel} tone="danger" />
      </div>

      <div className={styles.actions}>
        <button onClick={() => navigate('/admin/hotels/new')}><Plus size={16} /> إضافة فندق</button>
        <button onClick={() => navigate('/admin/room-types/new')}><Plus size={16} /> إضافة غرفة</button>
        <button onClick={exportReport}><BarChart3 size={16} /> تصدير التقرير</button>
        <button onClick={() => navigate('/admin/pricing-rules')}><Tag size={16} /> قواعد التسعير</button>
      </div>

      <div className={styles.chartGrid}>
        <article className={styles.panel}>
          <h2>الإيرادات حسب الشهر</h2>
          <div className={styles.barChart}>
            {(revenue.length ? revenue : Array.from({ length: 6 }, (_, index) => ({ month: `M${index + 1}`, revenue: 0 }))).slice(-6).map((item) => {
              const value = number(item.revenue || item.totalRevenue || item.amount);
              return (
                <div key={item.month || item.label} className={styles.barItem}>
                  <span style={{ height: `${Math.max(6, (value / maxRevenue) * 100)}%` }} />
                  <small>{item.month || item.label}</small>
                </div>
              );
            })}
          </div>
        </article>

        <article className={styles.panel}>
          <h2>حالة الحجوزات</h2>
          <div className={styles.donutList}>
            {(statuses.length ? statuses : ['CONFIRMED', 'PENDING', 'CANCELLED', 'WAITING'].map((status) => ({ status, count: 0 }))).map((item) => {
              const count = number(item.count || item.value);
              return (
                <div key={item.status || item.name} className={styles.statusRow}>
                  <span className={`${styles.statusDot} ${statusClass(item.status || item.name)}`} />
                  <strong>{item.status || item.name}</strong>
                  <small>{Math.round((count / statusTotal) * 100)}%</small>
                </div>
              );
            })}
          </div>
        </article>
      </div>

      <div className={styles.tableGrid}>
        <article className={styles.panel}>
          <h2>أحدث الحجوزات</h2>
          <table className={styles.table}>
            <thead><tr><th>اسم الضيف</th><th>رقم الغرفة / الغرفة</th><th>تاريخ الدخول</th><th>الحالة</th></tr></thead>
            <tbody>
              {(Array.isArray(bookings) ? bookings : []).slice(0, 10).map((booking) => (
                <tr key={booking.id}>
                  <td>{booking.guestName || booking.guest?.name || booking.guestEmail || '-'}</td>
                  <td>{booking.roomNumber || booking.roomTypeName || booking.room?.name || '-'}</td>
                  <td>{booking.checkInDate || booking.checkIn || '-'}</td>
                  <td><span className={`${styles.badge} ${statusClass(booking.status)}`}>{booking.status || '-'}</span></td>
                </tr>
              ))}
              {(!Array.isArray(bookings) || bookings.length === 0) && <tr><td colSpan="4">لا توجد حجوزات حديثة</td></tr>}
            </tbody>
          </table>
        </article>

        <article className={styles.panel}>
          <h2>الغرف الأكثر طلباً</h2>
          <table className={styles.table}>
            <thead><tr><th>اسم الغرفة</th><th>عدد الحجوزات</th><th>نسبة الإشغال</th></tr></thead>
            <tbody>
              {rooms.slice(0, 10).map((room) => (
                <tr key={room.roomTypeId || room.id || room.name}>
                  <td>{room.roomTypeName || room.name || '-'}</td>
                  <td>{number(room.bookingCount || room.bookingsCount)}</td>
                  <td>{number(room.occupancyRate).toFixed(0)}%</td>
                </tr>
              ))}
              {rooms.length === 0 && <tr><td colSpan="3">لا توجد بيانات غرف</td></tr>}
            </tbody>
          </table>
        </article>
      </div>
    </section>
  );
};

export default AdminDashboard;
