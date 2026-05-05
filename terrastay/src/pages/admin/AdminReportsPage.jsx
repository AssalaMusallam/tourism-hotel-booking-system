import { useState, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { BarChart2, ChevronDown } from 'lucide-react';
import { useAdminHotels } from '../../hooks/useCatalogQueries';
import { useRevenueReport, useOccupancyReport, usePopularRooms } from '../../hooks/useAdminReports';
import RevenueReportCard from '../../components/admin/reports/RevenueReportCard';
import OccupancyGauge from '../../components/admin/reports/OccupancyGauge';
import PopularRoomsChart from '../../components/admin/reports/PopularRoomsChart';
import ReportFilterBar from '../../components/admin/reports/ReportFilterBar';
import styles from './AdminReportsPage.module.css';

// ── Hotel selector ────────────────────────────────────────────────────────────

const HotelSelector = ({ hotels, selectedId, onChange }) => (
  <div className={styles.selectorWrap}>
    <div className={styles.selectBox}>
      <select
        className={styles.select}
        value={selectedId || ''}
        onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
      >
        <option value="">— Select a hotel to view reports —</option>
        {hotels.map((h) => (
          <option key={h.id} value={h.id}>{h.name}</option>
        ))}
      </select>
      <ChevronDown size={16} className={styles.selectIcon} />
    </div>
  </div>
);

// ── Page ──────────────────────────────────────────────────────────────────────

/**
 * Admin-only analytics & reports page.
 * Route: /dashboard/reports
 *
 * Revenue + occupancy load only after [Run] is clicked.
 * Popular rooms auto-loads on hotel change.
 */
const AdminReportsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const hotelId = searchParams.get('hotelId') ? Number(searchParams.get('hotelId')) : null;

  // Revenue run params (null = not yet run)
  const [revenueParams, setRevenueParams] = useState(null);
  // Occupancy run params (null = not yet run)
  const [occupancyParams, setOccupancyParams] = useState(null);

  // Fetch all hotels for the selector
  const { data: hotelsData } = useAdminHotels({ page: 0, size: 100 });
  const hotels = hotelsData?.content || [];

  // Report hooks
  const revenueQuery = useRevenueReport(
    hotelId, revenueParams?.from, revenueParams?.to,
    !!(hotelId && revenueParams)
  );
  const occupancyQuery = useOccupancyReport(
    hotelId, occupancyParams?.month,
    !!(hotelId && occupancyParams)
  );
  const popularQuery = usePopularRooms(hotelId, !!hotelId);

  // Handlers
  const handleHotelChange = useCallback((id) => {
    const next = new URLSearchParams();
    if (id) next.set('hotelId', String(id));
    setSearchParams(next, { replace: true });
    // Reset run params when hotel changes
    setRevenueParams(null);
    setOccupancyParams(null);
  }, [setSearchParams]);

  const handleRevenue = useCallback((from, to) => {
    setRevenueParams({ from, to });
  }, []);

  const handleOccupancy = useCallback((month) => {
    setOccupancyParams({ month });
  }, []);

  const retryRevenue   = () => setRevenueParams((p) => p ? { ...p } : null);
  const retryOccupancy = () => setOccupancyParams((p) => p ? { ...p } : null);
  const retryPopular   = () => popularQuery.refetch();

  return (
    <div className={styles.page}>
      {/* ── Sidebar ── */}
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin"                      className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels"               className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/amenities"            className={styles.navLink}>Manage Amenities</Link>
          <Link to="/dashboard/users"            className={styles.navLink}>Manage Users</Link>
          <Link to="/dashboard/pricing-rules"    className={styles.navLink}>Pricing Rules</Link>
          <Link to="/dashboard/reports"          className={`${styles.navLink} ${styles.active}`}>Analytics</Link>
        </nav>
      </aside>

      {/* ── Main ── */}
      <main className={styles.main}>
        {/* Page header */}
        <div className={styles.header}>
          <div className={styles.headerLeft}>
            <BarChart2 size={22} className={styles.headerIcon} />
            <div>
              <h1>Analytics &amp; Reports</h1>
              <p className={styles.headerSub}>Revenue, occupancy, and booking insights per hotel.</p>
            </div>
          </div>
        </div>

        {/* Hotel selector */}
        <HotelSelector
          hotels={hotels}
          selectedId={hotelId}
          onChange={handleHotelChange}
        />

        {!hotelId ? (
          <div className={styles.noHotel}>
            <BarChart2 size={40} className={styles.noHotelIcon} />
            <p>Select a hotel above to view its reports.</p>
          </div>
        ) : (
          <>
            {/* Filter bar */}
            <ReportFilterBar
              hotelId={hotelId}
              onRevenue={handleRevenue}
              onOccupancy={handleOccupancy}
            />

            {/* Revenue + Occupancy side by side */}
            <div className={styles.cardsRow}>
              <div className={styles.cardCell}>
                {revenueParams ? (
                  <RevenueReportCard
                    data={revenueQuery.data}
                    isLoading={revenueQuery.isLoading}
                    isError={revenueQuery.isError}
                    error={revenueQuery.error}
                    onRetry={retryRevenue}
                  />
                ) : (
                  <div className={styles.pendingCard}>
                    <span>💰</span>
                    <p>Set a date range and click <strong>Run</strong> to load the revenue report.</p>
                  </div>
                )}
              </div>

              <div className={styles.cardCell}>
                {occupancyParams ? (
                  <OccupancyGauge
                    data={occupancyQuery.data}
                    isLoading={occupancyQuery.isLoading}
                    isError={occupancyQuery.isError}
                    error={occupancyQuery.error}
                    onRetry={retryOccupancy}
                  />
                ) : (
                  <div className={styles.pendingCard}>
                    <span>🏨</span>
                    <p>Select a month and click <strong>Run</strong> to load occupancy data.</p>
                  </div>
                )}
              </div>
            </div>

            {/* Popular rooms — auto-loaded */}
            <PopularRoomsChart
              data={popularQuery.data}
              isLoading={popularQuery.isLoading}
              isError={popularQuery.isError}
              error={popularQuery.error}
              onRetry={retryPopular}
            />
          </>
        )}
      </main>
    </div>
  );
};

export default AdminReportsPage;
