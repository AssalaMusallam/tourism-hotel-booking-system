import { useMemo } from 'react';
import { useParams, useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { format, differenceInDays, parseISO, addDays } from 'date-fns';
import { MapPin, ArrowLeft, MessageCircleQuestion } from 'lucide-react';
import { getHotelById } from '../../api/hotelsApi';
import AvailabilitySearchForm from '../../components/availability/AvailabilitySearchForm';
import HotelAvailabilityList from '../../components/availability/HotelAvailabilityList';
import styles from './HotelAvailabilityPage.module.css';

const todayStr = format(new Date(), 'yyyy-MM-dd');
const tomorrowStr = format(addDays(new Date(), 1), 'yyyy-MM-dd');

/**
 * Full hotel availability page.
 * Route: /hotels/:hotelId/availability
 * URL is the single source of truth for all filter state.
 */
const HotelAvailabilityPage = () => {
  const { hotelId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  // ── Read URL params ─────────────────────────────────────────────────────────
  const checkIn       = searchParams.get('checkIn')       || todayStr;
  const checkOut      = searchParams.get('checkOut')      || tomorrowStr;
  const guests        = searchParams.get('guests')        ? Number(searchParams.get('guests')) : undefined;
  const availableOnly = searchParams.get('availableOnly') === 'true';
  const q             = searchParams.get('q')             || undefined;
  const sort          = searchParams.get('sort')          || 'basePrice,asc';

  // ── Hotel data ──────────────────────────────────────────────────────────────
  const { data: hotel } = useQuery({
    queryKey: ['hotel', hotelId],
    queryFn: () => getHotelById(hotelId),
    enabled: !!hotelId,
    staleTime: 10 * 60 * 1000,
  });

  // ── Derived date strip ──────────────────────────────────────────────────────
  const dateStrip = useMemo(() => {
    if (!checkIn || !checkOut) return null;
    try {
      const nights = Math.max(0, differenceInDays(parseISO(checkOut), parseISO(checkIn)));
      const start  = format(parseISO(checkIn),  'MMM d');
      const end    = format(parseISO(checkOut), 'MMM d, yyyy');
      const gStr   = guests ? ` · ${guests} guest${guests === 1 ? '' : 's'}` : '';
      return `${start} – ${end} · ${nights} night${nights === 1 ? '' : 's'}${gStr}`;
    } catch {
      return null;
    }
  }, [checkIn, checkOut, guests]);

  // ── URL update on search ────────────────────────────────────────────────────
  const handleSearch = (params) => {
    const next = new URLSearchParams();
    if (params.checkIn)       next.set('checkIn',       params.checkIn);
    if (params.checkOut)      next.set('checkOut',      params.checkOut);
    if (params.guests)        next.set('guests',        String(params.guests));
    if (params.q)             next.set('q',             params.q);
    if (params.availableOnly) next.set('availableOnly', 'true');
    if (params.sort)          next.set('sort',          params.sort);
    setSearchParams(next, { replace: true });
  };

  // ── Book Now navigation ─────────────────────────────────────────────────────
  const handleRoomSelect = (roomTypeId) => {
    const params = new URLSearchParams();
    params.set('checkIn',  checkIn);
    params.set('checkOut', checkOut);
    if (guests) params.set('guests', String(guests));
    navigate(`/hotels/${hotelId}/rooms/${roomTypeId}/book?${params.toString()}`);
  };

  // ── Initial search form values ──────────────────────────────────────────────
  const initialValues = { checkIn, checkOut, guests, q, availableOnly, sort };

  return (
    <main className={styles.page}>
      {/* ── Hotel summary header ── */}
      <section className={styles.hero}>
        <div className={styles.heroInner}>
          <Link to={`/hotels/${hotelId}`} className={styles.backLink}>
            <ArrowLeft size={16} /> Back to hotel
          </Link>
          <div className={styles.heroText}>
            <span className={styles.eyebrow}>Availability &amp; pricing</span>
            <h1 className={styles.hotelName}>
              {hotel?.name || 'Hotel Availability'}
            </h1>
            {(hotel?.city || hotel?.country) && (
              <p className={styles.location}>
                <MapPin size={14} />
                {[hotel.city, hotel.country].filter(Boolean).join(', ')}
              </p>
            )}
          </div>
        </div>
      </section>

      {/* ── Search form ── */}
      <section className={styles.formSection}>
        <div className={styles.container}>
          <AvailabilitySearchForm
            hotelId={Number(hotelId)}
            onSearch={handleSearch}
            initialValues={initialValues}
          />
        </div>
      </section>

      {/* ── Date summary strip ── */}
      {dateStrip && (
        <div className={styles.dateStrip}>
          <div className={styles.container}>
            <span className={styles.dateStripText}>{dateStrip}</span>
          </div>
        </div>
      )}

      {/* ── Room list ── */}
      <section className={styles.listSection}>
        <div className={styles.container}>
          <HotelAvailabilityList
            hotelId={Number(hotelId)}
            checkIn={checkIn}
            checkOut={checkOut}
            guests={guests}
            q={q}
            availableOnly={availableOnly}
            sort={sort}
            onRoomSelect={handleRoomSelect}
          />
        </div>
      </section>

      {/* ── Footer nudge ── */}
      <section className={styles.footer}>
        <div className={styles.container}>
          <div className={styles.nudge}>
            <MessageCircleQuestion size={18} />
            <span>Can&rsquo;t find what you&rsquo;re looking for?</span>
            <Link to={`/hotels/${hotelId}`} className={styles.nudgeLink}>
              View hotel details →
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
};

export default HotelAvailabilityPage;
