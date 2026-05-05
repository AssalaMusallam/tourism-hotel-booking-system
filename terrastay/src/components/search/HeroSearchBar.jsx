import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, Users, Search } from 'lucide-react';
import { useCities } from '../../hooks/useCatalogQueries';
import { addDays, format } from 'date-fns';
import styles from './HeroSearchBar.module.css';

const toInputDate = (d) => format(d, 'yyyy-MM-dd');

const HeroSearchBar = ({ compact = false, onSearch, defaultValues = {} }) => {
  const navigate = useNavigate();
  const { data: citiesList } = useCities();
  const cities = citiesList || [];

  const [city, setCity] = useState(defaultValues.city || '');
  const [checkIn, setCheckIn] = useState(defaultValues.checkIn || toInputDate(addDays(new Date(), 1)));
  const [checkOut, setCheckOut] = useState(defaultValues.checkOut || toInputDate(addDays(new Date(), 4)));
  const [guests, setGuests] = useState(defaultValues.guests || 2);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const suggestRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (suggestRef.current && !suggestRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleCityInput = (val) => {
    setCity(val);
    if (val.trim() && cities.length) {
      const filtered = cities.filter((c) => c.toLowerCase().includes(val.toLowerCase())).slice(0, 8);
      setSuggestions(filtered);
      setShowSuggestions(filtered.length > 0);
    } else {
      setShowSuggestions(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (checkOut && checkIn && checkOut <= checkIn) return;

    const params = new URLSearchParams();
    if (city) params.set('city', city);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', guests);

    if (onSearch) {
      onSearch({ city, checkIn, checkOut, guests });
    } else {
      navigate(`/search?${params.toString()}`);
    }
  };

  return (
    <form
      onSubmit={handleSearch}
      className={`${styles.form} ${compact ? styles.compact : ''}`}
    >
      <div className={styles.field} ref={suggestRef}>
        <MapPin size={16} className={styles.icon} />
        <input
          type="text"
          value={city}
          onChange={(e) => handleCityInput(e.target.value)}
          onFocus={() => city && suggestions.length && setShowSuggestions(true)}
          className={styles.textInput}
          placeholder="City or destination"
          aria-label="City"
        />
        {showSuggestions && (
          <div className={styles.suggestions}>
            {suggestions.map((s) => (
              <button
                type="button"
                key={s}
                className={styles.suggestion}
                onClick={() => { setCity(s); setShowSuggestions(false); }}
              >
                <MapPin size={12} /> {s}
              </button>
            ))}
          </div>
        )}
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
