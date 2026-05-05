import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, Users, Search } from 'lucide-react';
import { CITIES } from '../../constants/cities';
import { toInputDate } from '../../utils/formatDate';
import { addDays } from 'date-fns';
import styles from './HeroSearchBar.module.css';

const HeroSearchBar = ({ compact = false, initialValues = {} }) => {
  const navigate = useNavigate();
  const [city, setCity] = useState(initialValues.city || '');
  const [checkIn, setCheckIn] = useState(initialValues.checkIn || toInputDate(addDays(new Date(), 1)));
  const [checkOut, setCheckOut] = useState(initialValues.checkOut || toInputDate(addDays(new Date(), 4)));
  const [guests, setGuests] = useState(initialValues.guests || 2);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (city) params.set('city', city);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', guests);
    navigate(`/search?${params.toString()}`);
  };

  return (
    <form
      onSubmit={handleSearch}
      className={`${styles.form} ${compact ? styles.compact : ''}`}
    >
      <div className={styles.field}>
        <MapPin size={16} className={styles.icon} />
        <select
          value={city}
          onChange={(e) => setCity(e.target.value)}
          className={styles.select}
          aria-label="City"
        >
          <option value="">All Cities</option>
          {CITIES.map((c) => (
            <option key={c.value} value={c.value}>{c.label}</option>
          ))}
        </select>
      </div>

      <div className={styles.divider} />

      <div className={styles.field}>
        <Calendar size={16} className={styles.icon} />
        <div className={styles.dateGroup}>
          <input
            type="date"
            value={checkIn}
            onChange={(e) => setCheckIn(e.target.value)}
            min={toInputDate(new Date())}
            className={styles.dateInput}
            aria-label="Check-in date"
          />
          <span className={styles.dateSep}>→</span>
          <input
            type="date"
            value={checkOut}
            onChange={(e) => setCheckOut(e.target.value)}
            min={checkIn || toInputDate(addDays(new Date(), 1))}
            className={styles.dateInput}
            aria-label="Check-out date"
          />
        </div>
      </div>

      <div className={styles.divider} />

      <div className={styles.field}>
        <Users size={16} className={styles.icon} />
        <div className={styles.guestControl}>
          <button
            type="button"
            onClick={() => setGuests((g) => Math.max(1, g - 1))}
            className={styles.guestBtn}
            aria-label="Decrease guests"
          >−</button>
          <span className={styles.guestCount}>{guests} Guest{guests !== 1 ? 's' : ''}</span>
          <button
            type="button"
            onClick={() => setGuests((g) => Math.min(10, g + 1))}
            className={styles.guestBtn}
            aria-label="Increase guests"
          >+</button>
        </div>
      </div>

      <button type="submit" className={styles.searchBtn}>
        <Search size={18} />
        <span>Search</span>
      </button>
    </form>
  );
};

export default HeroSearchBar;
